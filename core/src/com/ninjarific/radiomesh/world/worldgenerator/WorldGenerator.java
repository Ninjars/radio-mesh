package com.ninjarific.radiomesh.world.worldgenerator;

import com.ninjarific.radiomesh.coordinates.Bounds;
import com.ninjarific.radiomesh.coordinates.Coordinate;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;
import com.ninjarific.radiomesh.world.data.Center;
import com.ninjarific.radiomesh.world.data.Corner;
import com.ninjarific.radiomesh.world.data.Edge;
import com.ninjarific.radiomesh.world.data.MapProperties;
import com.ninjarific.radiomesh.world.logger.LoadingLogger;
import com.ninjarific.radiomesh.world.worldgenerator.biomes.AridBiomeMapper;
import com.ninjarific.radiomesh.world.worldgenerator.biomes.IBiomeMapper;
import com.ninjarific.radiomesh.world.worldgenerator.biomes.TemperateBiomeMapper;
import com.ninjarific.radiomesh.world.worldgenerator.biomes.TundralBiomeMapper;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    private static final String TAG = WorldGenerator.class.getSimpleName();
    private static final int WORLD_SIZE = 100;
    private static final int POINT_COUNT = 8000;
    private static final int MAX_RIVERS_PER_THOUSAND_POINTS = 10;
    private static final double LAKE_THRESHOLD = 0.3;
    private static final double MOUNTAIN_SCALE_FACTOR = 1.1; // > 1 to increase the amount of maxed-out mountain tops
    private static final double MOISTURE_DROPOFF = 0.95; // factor applied to each step away from a freshwater source

    public static MapData generateWorld(NodeData nodeData, LoadingLogger logger) {
        logger.start();
        final long seed = nodeData.getSeed();
        logger.beginningStage("voronoi graph");
        VoronoiResults voronoiResults = generateVoronoi(seed);
        logger.completedStage("voronoi graph");

        RectD clippingBoundRect = voronoiResults.clippingBounds;
        Bounds bounds = new Bounds(0, 0, clippingBoundRect.width(), clippingBoundRect.height());

        logger.beginningStage("generate centers");
        PointD[] centerPoints = voronoiResults.generatorSites;
        List<Center> centers = WorldGeneratorUtils.createCenters(centerPoints, clippingBoundRect);
        logger.completedStage("generate centers");

        logger.beginningStage("generate corners");
        PointD[] cornerPoints = voronoiResults.voronoiVertices;
        List<Corner> corners = WorldGeneratorUtils.createCorners(cornerPoints, clippingBoundRect);
        logger.completedStage("generate corners");

        logger.beginningStage("generate edges");
        List<Edge> edges = WorldGeneratorUtils.createEdges(voronoiResults.voronoiEdges, centers, corners);
        logger.completedStage("generate edges");

        logger.beginningStage("improve corners");
        WorldGeneratorUtils.improveCorners(corners);
        logger.completedStage("improve corners");

        logger.beginningStage("corner properties");
        final int maxDimension = (int) Math.ceil(Math.max(bounds.getWidth(), bounds.getHeight()));
        IslandShape shape = new PerlinIslandShape(seed, maxDimension);
        for (Corner corner : corners) {
            corner.setWorldBorder(corner.getTouches().size() <= 2);
            MapProperties properties = new MapProperties();
            if (corner.isWorldBorder()) {
                properties.setType(MapProperties.Type.BORDER_OCEAN);
            } else if (!isInsideShape(maxDimension, shape, corner.position)) {
                properties.setType(MapProperties.Type.LAKE);
            }
            corner.setMapProperties(properties);
        }
        logger.completedStage("corner properties");

        logger.beginningStage("center properties");
        List<Center> borderCenters = new ArrayList<>();
        for (Center center : centers) {
            MapProperties centerProperties = new MapProperties();
            int waterCornerCount = 0;
            for (Corner corner : center.getCorners()) {
                if (corner.isWorldBorder()) {
                    centerProperties.setType(MapProperties.Type.BORDER_OCEAN);
                    borderCenters.add(center);
                    continue;
                }
                MapProperties.Type cornerType = corner.getMapProperties().getType();
                if (cornerType == MapProperties.Type.BORDER_OCEAN || cornerType == MapProperties.Type.LAKE) {
                    waterCornerCount++;
                }
            }
            if (centerProperties.getType() != MapProperties.Type.BORDER_OCEAN
                    && waterCornerCount >= center.getCorners().size() * LAKE_THRESHOLD) {
                centerProperties.setType(MapProperties.Type.LAKE);
            }
            center.setMapProperties(centerProperties);
        }
        logger.completedStage("center properties");

        logger.beginningStage("ocean borders");
        floodFillCenterOceanProperty(borderCenters);
        logger.completedStage("ocean borders");

        logger.beginningStage("ocean centers");
        markCenterOceanProperties(centers);
        logger.completedStage("ocean centers");

        logger.beginningStage("ocean corners");
        markCornerOceanProperties(corners);
        logger.completedStage("ocean corners");

        logger.beginningStage("corner elevations");
        assignCornerElevations(corners);
        logger.completedStage("corner elevations");

        logger.beginningStage("redistribute elevations");
        redistributeElevation(corners);
        logger.completedStage("redistribute elevations");

        logger.beginningStage("flatten oceans");
        flattenOceans(corners);
        logger.completedStage("flatten oceans");

        logger.beginningStage("center elevations");
        assignCenterElevations(centers);
        logger.completedStage("center elevations");

        logger.beginningStage("generate moisture");
        calculateDownslopes(corners);
        generateRiver(seed, corners);
        assignCornerMoisture(corners);
        assignCenterMoisture(centers);
        logger.completedStage("generate moisture");

        logger.beginningStage("assign biomes");
        assignBiomes(seed, centers);
        logger.completedStage("assign biomes");

        logger.end();
        return new MapData(seed, centers, corners, edges, bounds);
    }

    private static void assignBiomes(long seed, List<Center> centers) {
        Random random = new Random(seed);
        IBiomeMapper biomeMapper;
        switch (random.nextInt(3)) {
            case 0:
                biomeMapper = new TundralBiomeMapper();
                break;
            case 1:
                biomeMapper = new AridBiomeMapper();
                break;
            case 2:
            default:
                biomeMapper = new TemperateBiomeMapper();
                break;
        }
        for (Center c : centers) {
            c.getMapProperties().setBiome(biomeMapper.getBiome(c.getMapProperties()));
        }
    }

    private static void generateRiver(long seed, List<Corner> corners) {
        Random random = new Random(seed);
        int riverCount = random.nextInt(MAX_RIVERS_PER_THOUSAND_POINTS * POINT_COUNT/1000);
        List<Corner> validRiverStarts = new ArrayList<>(corners.size() / 2);
        for (Corner corner : corners) {
            MapProperties properties = corner.getMapProperties();
            if (properties.getType() != MapProperties.Type.BORDER_OCEAN
                    && properties.getElevation() > 0.3
                    && properties.getElevation() < 0.9) {
                validRiverStarts.add(corner);
            }
        }
        for (int i = 0; i < riverCount; i++) {
            Corner corner = validRiverStarts.get(random.nextInt(validRiverStarts.size()));
            while (corner.getMapProperties().getType() != MapProperties.Type.BORDER_OCEAN) {
                if (corner == corner.getDownslope()) {
                    break;
                }
                Edge edge = lookupEdgeFromCorner(corner, corner.getDownslope());
                if (edge == null) {
                    break;
                }
                edge.incrementRiverValue();
                corner.getMapProperties().incrementRiver();
                corner.getDownslope().getMapProperties().incrementRiver();
                corner = corner.getDownslope();
            }
        }
    }

    private static Edge lookupEdgeFromCorner(Corner corner, Corner downslope) {
        for (Edge edge : corner.getProtrudes()) {
            if (edge.v0 == downslope || edge.v1 == downslope) {
                return edge;
            }
        }
        return null;
    }

    private static void calculateDownslopes(List<Corner> corners) {
        for (Corner corner : corners) {
            Corner downslope = corner;
            for (Corner other : corner.getAdjacent()) {
                if (other.getMapProperties().getElevation() < downslope.getMapProperties().getElevation()) {
                    downslope = other;
                }
            }
            corner.setDownslope(downslope);
        }
    }

    private static void assignCornerMoisture(List<Corner> corners) {
        List<Corner> queue = new ArrayList<>(corners.size() / 2);
        // fresh water propagation
        for (Corner corner : corners) {
            MapProperties properties = corner.getMapProperties();
            if (properties.isFreshWater()) {
                int riverValue = properties.getRiverValue();
                properties.setMoisture(riverValue > 0 ? Math.min(3.0, (0.2 * riverValue)) : 1.0);
                queue.add(corner);
            } else {
                properties.setMoisture(0);
            }
        }
        for (int i = 0; i < queue.size(); i++) {
            Corner corner = queue.get(i);
            double moisture = corner.getMapProperties().getMoisture() * MOISTURE_DROPOFF;
            for (Corner adj : corner.getAdjacent()) {
                if (moisture > adj.getMapProperties().getMoisture()) {
                    adj.getMapProperties().setMoisture(moisture);
                    queue.add(adj);
                }
            }
        }
        // salt water and cap values
        for (Corner corner : corners) {
            MapProperties mapProperties = corner.getMapProperties();
            if (mapProperties.isSaltWater()) {
                mapProperties.setMoisture(1);
            } else if (mapProperties.getMoisture() > 1) {
                mapProperties.setMoisture(1);
            }
        }
    }

    private static void assignCenterMoisture(List<Center> centers) {
        for (Center center : centers) {
            double sum = 0;
            for (Corner corner : center.getCorners()) {
                sum += corner.getMapProperties().getMoisture();
            }
            center.getMapProperties().setMoisture(sum / (double) center.getCorners().size());
        }
    }

    private static void flattenOceans(List<Corner> corners) {
        for (Corner corner : corners) {
            MapProperties properties = corner.getMapProperties();
            if (properties.isOcean() || properties.getType().equals(MapProperties.Type.COAST)) {
                properties.setElevation(0);
            }
        }
    }

    /**
     * Ocean has a height of 0, then the land climbs up from the boarders to be highest furthest from the coast
     */
    private static void assignCornerElevations(List<Corner> corners) {
        List<Corner> queue = new ArrayList<>();
        for (Corner corner : corners) {
            MapProperties properties = corner.getMapProperties();
            if (corner.isWorldBorder()) {
                properties.setElevation(0.0);
                queue.add(corner);
            } else {
                properties.setElevation(Double.MAX_VALUE);
            }
        }

        int i  = 0;
        while (i < queue.size()) {
            Corner corner = queue.get(i);
            double cornerElevation = corner.getMapProperties().getElevation();
            boolean cornerIsLand = corner.getMapProperties().isLand();
            for (Corner adjacent : corner.getAdjacent()) {
                MapProperties adjacentProperties = adjacent.getMapProperties();
                double newElevation = 0.01 + cornerElevation;
                if (cornerIsLand && adjacentProperties.isLand()) {
                    // lakes will be considered essentially flat, land will slope away from coasts
                    newElevation += 1;
                }
                // check to see if we've got a new height for this corner, and if so add it to the queue
                if (newElevation < adjacentProperties.getElevation()) {
                    adjacentProperties.setElevation(newElevation);
                    queue.add(adjacent);
                }
            }
            i++;
        }
    }

    private static void redistributeElevation(List<Corner> corners) {
        List<Corner> sortedLandCorners = new ArrayList<>(corners.size());
        for (Corner c : corners) {
            if (c.getMapProperties().isLand() || c.getMapProperties().getType().equals(MapProperties.Type.LAKE)) {
                sortedLandCorners.add(c);
            }
        }
        Collections.sort(sortedLandCorners,
                (a, b) -> Double.compare(a.getMapProperties().getElevation(), b.getMapProperties().getElevation()));

        double fraction = 1 / (double)(sortedLandCorners.size() - 1);
        double scaleFactorRoot = Math.sqrt(MOUNTAIN_SCALE_FACTOR);
        for (int i = 0; i < sortedLandCorners.size(); i++) {
            // Let y(x) be the total area that we want at elevation <= x.
            // We want the higher elevations to occur less than lower
            // ones, and set the area to be y(x) = 1 - (1-x)^2.
            double y = i * fraction;
            double x = scaleFactorRoot - Math.sqrt(MOUNTAIN_SCALE_FACTOR * (1-y));
            if (x > 1.0) {
                x = 1.0;
            }
            sortedLandCorners.get(i).getMapProperties().setElevation(x);
        }
    }

    /**
     * Centers have the elevations that's the average of their corners
     */
    private static void assignCenterElevations(List<Center> centers) {
        for (Center center : centers) {
            double sum = 0;
            for (Corner corner : center.getCorners()) {
                sum += corner.getMapProperties().getElevation();
            }
            center.getMapProperties().setElevation(sum / (double) center.getCorners().size());
        }
    }

    private static boolean isInsideShape(int maxDimension, IslandShape shape, Coordinate coordinate) {
        return shape.isInside(
                2 * (coordinate.x / (double) maxDimension - 0.5),
                2 * (coordinate.y / (double) maxDimension - 0.5));
    }

    private static void floodFillCenterOceanProperty(List<Center> oceanBoarders) {
        int i = 0;
        while (i < oceanBoarders.size()) {
            Center c = oceanBoarders.get(i);
            for (Center other : c.getNeighbours()) {
                MapProperties properties = other.getMapProperties();
                if (properties.getType() == MapProperties.Type.LAKE) {
                    properties.setType(MapProperties.Type.BORDER_OCEAN);
                    oceanBoarders.add(other);
                }
            }
            i++;
        }
    }

    private static void markCenterOceanProperties(List<Center> centers) {
        for (Center center : centers) {
            int oceans = 0;
            int lands = 0;
            for (Center c : center.getNeighbours()) {
                MapProperties properties = c.getMapProperties();
                if (properties.isLand()) {
                    lands++;
                } else if (properties.isOcean()) {
                    oceans++;
                }
                if (oceans > 0 && lands > 0) {
                    break;
                }
            }
            MapProperties centerProperties = center.getMapProperties();
            if (oceans > 0 && lands > 0) {
                if (centerProperties.getType() == MapProperties.Type.LAND) {
                    centerProperties.setType(MapProperties.Type.COAST);
                } else {
                    centerProperties.setType(MapProperties.Type.SHALLOWS);
                }
            }
        }
    }

    private static void markCornerOceanProperties(List<Corner> corners) {
        for (Corner corner : corners) {
            int oceans = 0;
            int lands = 0;
            for (Center c : corner.getTouches()) {
                MapProperties properties = c.getMapProperties();
                if (properties.isLand()) {
                    lands++;
                } else if (properties.isOcean()) {
                    oceans++;
                }
                if (oceans > 0 && lands > 0) {
                    break;
                }
            }
            int numberOfCenters = corner.getTouches().size();
            MapProperties properties = corner.getMapProperties();
            if (oceans == numberOfCenters) {
                properties.setType(MapProperties.Type.BORDER_OCEAN);
            } else if (oceans > 0 && lands > 0) {
                properties.setType(MapProperties.Type.COAST);
            } else if (lands != numberOfCenters) {
                properties.setType(MapProperties.Type.LAKE);
            }
        }
    }

    private static VoronoiResults generateVoronoi(long seed) {
        Random random = new Random(seed);

        PointD[] points = new PointD[POINT_COUNT];
        for (int i = 0; i < points.length; i++) {
            PointD point = new PointD(random.nextDouble() * WORLD_SIZE, random.nextDouble() * WORLD_SIZE);
            points[i] = point;
        }
        VoronoiResults voronoiGraph = Voronoi.findAll(points);
        voronoiGraph = performLloydRelaxation(voronoiGraph);
        return performLloydRelaxation(voronoiGraph);
    }

    private static VoronoiResults performLloydRelaxation(VoronoiResults graph) {
        PointD[][] regions = graph.voronoiRegions();
        PointD[] approxCenters = new PointD[regions.length];
        for (int i = 0; i < regions.length; i++) {
            PointD[] region = regions[i];
            double x = 0, y = 0;
            for (PointD point : region) {
                x += point.x;
                y += point.y;
            }
            x /= (float) region.length;
            y /= (float) region.length;
            approxCenters[i] = new PointD(x, y);
        }
        return Voronoi.findAll(approxCenters);
    }
}
