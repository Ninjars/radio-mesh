package com.ninjarific.radiomesh.world.worldgenerator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.ninjarific.radiomesh.coordinates.Bounds;
import com.ninjarific.radiomesh.coordinates.Coordinate;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;
import com.ninjarific.radiomesh.world.WorldColors;
import com.ninjarific.radiomesh.world.data.Center;
import com.ninjarific.radiomesh.world.data.Corner;
import com.ninjarific.radiomesh.world.data.Edge;
import com.ninjarific.radiomesh.world.data.MapPiece;
import com.ninjarific.radiomesh.world.data.MapProperties;
import com.ninjarific.radiomesh.world.logger.LoadingLogger;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    private static final int WORLD_SIZE = 100;
    private static final int POINT_COUNT = 8000;
    private static final double LAKE_THRESHOLD = 0.3;
    private static final double MOUNTAIN_SCALE_FACTOR = 1.1; // > 1 to increase the amount of maxed-out mountain tops
    private static final boolean DEBUG_HEIGHTMAP = false;

    public static WorldModel generateWorld(NodeData nodeData, LoadingLogger logger) {
        logger.start();
        final long seed = nodeData.getSeed();
        logger.beginningStage("voronoi graph");
        VoronoiResults voronoiResults = generateVoronoi(seed, new Bounds(0, 0, WORLD_SIZE, WORLD_SIZE));
        logger.completedStage("voronoi graph");

        RectD clippingBoundRect = voronoiResults.clippingBounds;
        double xOffset = -clippingBoundRect.min.x;
        double yOffset = -clippingBoundRect.min.y;
        Bounds bounds = new Bounds(0, 0, clippingBoundRect.width(), clippingBoundRect.height());

        logger.beginningStage("generate centers");
        PointD[] centerPoints = voronoiResults.generatorSites;
        List<Center> centers = new ArrayList<>(centerPoints.length);
        for (int i = 0; i < centerPoints.length; i++) {
            PointD position = centerPoints[i];
            Center center = new Center(i, new Coordinate(position.x + xOffset, position.y + yOffset));
            centers.add(center);
        }
        logger.completedStage("generate centers");

        logger.beginningStage("generate corners");
        PointD[] cornerPoints = voronoiResults.voronoiVertices;
        List<Corner> corners = new ArrayList<>(centerPoints.length);
        for (int i = 0; i < cornerPoints.length; i++) {
            PointD position = cornerPoints[i];
            double x = position.x + xOffset;
            double y = position.y + yOffset;
            Corner corner = new Corner(i, new Coordinate(x, y));
            corner.setWorldBorder(x <= bounds.left
                    || x >= bounds.right
                    || y <= bounds.top
                    || y >= bounds.bottom);
            corners.add(corner);
        }
        logger.completedStage("generate corners");

        List<Edge> edges = new ArrayList<>();

        logger.startLoop("generate regions");
        for (int i = 0; i < voronoiResults.voronoiEdges.length; i++) {
            logger.startLoopIteration("generate regions");
            VoronoiEdge resultEdge = voronoiResults.voronoiEdges[i];
            Center centerA = centers.get(resultEdge.site1);
            Center centerB = centers.get(resultEdge.site2);
            Corner cornerA = corners.get(resultEdge.vertex1);
            Corner cornerB = corners.get(resultEdge.vertex2);
            edges.add(makeEdge(edges.size(), cornerA, cornerB, centerA, centerB));
            logger.endLoopIteration("generate regions");
        }
        logger.endLoop("generate regions");

        logger.beginningStage("improve corners");
        improveCorners(corners);
        logger.completedStage("improve corners");

        logger.beginningStage("corner properties");
        int maxDimension = (int) Math.ceil(Math.max(bounds.getWidth(), bounds.getHeight()));
        IslandShape shape = new PerlinIslandShape(seed, maxDimension);
        for (Corner corner : corners) {
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

        logger.beginningStage("create map pieces");
        List<MapPiece> map = createMapPieces(seed, centers);
        logger.completedStage("create map pieces");
        logger.end();
        return new WorldModel(map, bounds, centers, corners, edges);
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

    private static List<MapPiece> createMapPieces(long colorSeed, List<Center> centers) {
        Random colorRandom = new Random(colorSeed);
        List<MapPiece> map = new ArrayList<>(centers.size());
        for (Center center : centers) {
            MapProperties properties = center.getMapProperties();
            Color color = getColorForMapProperties(colorRandom, properties);
            map.add(new MapPiece(center, color));
        }
        return map;
    }

    private static Color getColorForMapProperties(Random colorRandom, MapProperties properties) {
        if (DEBUG_HEIGHTMAP) {
            double elevation = properties.getElevation();
            return WorldColors.getHeightMapColor(colorRandom, elevation);
        } else {
            switch (properties.getType()) {
                case LAND:
                    return WorldColors.getLandColor(colorRandom, properties.getElevation());
                case BORDER_OCEAN:
                    return WorldColors.getOceanColor(colorRandom);
                case COAST:
                    return WorldColors.getCoastColor(colorRandom);
                case SHALLOWS:
                    return WorldColors.getShallowsColor(colorRandom);
                case LAKE:
                    return WorldColors.getLakeColor(colorRandom);
                default:
                    Gdx.app.debug("WoldGenerator", "unhandled map property " + properties.getType());
                    return WorldColors.UNASSIGNED_COLOR;
            }
        }
    }

    private static VoronoiResults generateVoronoi(long seed, Bounds bounds) {
        Random random = new Random(seed);

        PointD[] points = new PointD[POINT_COUNT];
        for (int i = 0; i < points.length; i++) {
            PointD point = new PointD(random.nextDouble() * WORLD_SIZE, random.nextDouble() * WORLD_SIZE);
            points[i] = point;
        }
        RectD clippingRect = new RectD(bounds.left, bounds.top, bounds.right, bounds.bottom);
        VoronoiResults voronoiGraph = Voronoi.findAll(points, clippingRect);
        voronoiGraph = performLloydRelaxation(voronoiGraph, voronoiGraph.clippingBounds);
        return performLloydRelaxation(voronoiGraph, voronoiGraph.clippingBounds);
    }

    private static VoronoiResults performLloydRelaxation(VoronoiResults graph, RectD clippingRect) {
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
        return Voronoi.findAll(approxCenters, clippingRect);
    }

    private static void improveCorners(List<Corner> corners) {
        for (Corner corner : corners) {
            if (corner.isWorldBorder()) {
                continue;
            }
            double x = 0, y = 0;
            for (Center center : corner.getTouches()) {
                x += center.position.x;
                y += center.position.y;
            }
            x /= corner.getTouches().size();
            y /= corner.getTouches().size();
            corner.setPosition(new Coordinate(x, y));
        }
    }

    private static Edge makeEdge(int index, Corner cornerA, Corner cornerB, Center a, Center b) {
        Edge edge = new Edge(index, a, b, cornerA, cornerB);

        // connect centers and corners
        edge.v0.addEdge(edge);
        edge.v1.addEdge(edge);

        // connect centers
        if (edge.d0 != null && edge.d1 != null) {
            edge.d0.addNeighbour(edge.d1);
            edge.d1.addNeighbour(edge.d0);
        }

        // connect corners
        edge.v0.addAdjacent(edge.v1);
        edge.v1.addAdjacent(edge.v0);

        // corners reference centers
        edge.v0.addCenter(edge.d0);
        edge.v0.addCenter(edge.d1);
        edge.v1.addCenter(edge.d0);
        edge.v1.addCenter(edge.d1);

        return edge;
    }
}
