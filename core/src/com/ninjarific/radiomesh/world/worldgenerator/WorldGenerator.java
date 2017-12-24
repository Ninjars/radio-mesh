package com.ninjarific.radiomesh.world.worldgenerator;

import com.badlogic.gdx.graphics.Color;
import com.ninjarific.radiomesh.coordinates.Bounds;
import com.ninjarific.radiomesh.coordinates.Coordinate;
import com.ninjarific.radiomesh.coordinates.MutableBounds;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;
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
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    private static final int WORLD_SIZE = 100;
    private static final int POINT_COUNT = 8000;
    private static final double LAKE_THRESHOLD = 0.3;

    public static WorldModel generateWorld(NodeData nodeData, LoadingLogger logger) {
        logger.start();
        final long seed = nodeData.getSeed();
        logger.beginningStage("voronoi graph");
        VoronoiResults voronoiResults = generateVoronoi(seed, new MutableBounds(0, 0, WORLD_SIZE, WORLD_SIZE));
        logger.completedStage("voronoi graph");

        logger.beginningStage("generate centers");
        PointD[] centerPoints = voronoiResults.generatorSites;
        List<Center> centers = new ArrayList<>(centerPoints.length);
        for (int i = 0; i < centerPoints.length; i++) {
            PointD position = centerPoints[i];
            Center center = new Center(i, new Coordinate(position.x, position.y));
            centers.add(center);
        }
        logger.completedStage("generate centers");

        RectD clippingBoundRect = voronoiResults.clippingBounds;
        Bounds bounds = new Bounds(clippingBoundRect.min.x, clippingBoundRect.min.y, clippingBoundRect.max.x, clippingBoundRect.max.y);

        logger.beginningStage("generate corners");
        PointD[] cornerPoints = voronoiResults.voronoiVertices;
        List<Corner> corners = new ArrayList<>(centerPoints.length);
        for (int i = 0; i < cornerPoints.length; i++) {
            PointD position = cornerPoints[i];
            Corner corner = new Corner(i, new Coordinate(position.x, position.y));
            corner.setWorldBorder(position.x == bounds.left
                    || position.x == bounds.right
                    || position.y == bounds.top
                    || position.y == bounds.bottom);
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
        IslandShape shape = new RadialIslandShape(seed);
        for (Corner corner : corners) {
            MapProperties properties = new MapProperties();
            properties.setIsBorder(corner.isWorldBorder());
            properties.setIsWater(!isInsideShape(shape, corner.position) || corner.isWorldBorder());
            properties.setIsOcean(corner.isWorldBorder());
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
                    centerProperties.setIsBorder(true);
                    centerProperties.setIsOcean(true);
                    borderCenters.add(center);
                }
                if (corner.getMapProperties().isWater()) {
                    waterCornerCount++;
                }
            }
            boolean water = centerProperties.isOcean()
                    || waterCornerCount >= center.getCorners().size() * LAKE_THRESHOLD;
            centerProperties.setIsWater(water);
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

        logger.beginningStage("create map pieces");
        List<MapPiece> map = createMapPieces(centers);
        logger.completedStage("create map pieces");
        logger.end();
        return new WorldModel(map, bounds, centers, corners, edges);
    }

    private static boolean isInsideShape(IslandShape shape, Coordinate coordinate) {
        return shape.isInside(new Coordinate(
                2 * (coordinate.x / (double) WORLD_SIZE - 0.5),
                2 * (coordinate.y / (double) WORLD_SIZE - 0.5)));
    }

    private static void floodFillCenterOceanProperty(List<Center> oceanBoarders) {
        int i = 0;
        while (i < oceanBoarders.size()) {
            Center c = oceanBoarders.get(i);
            for (Center other : c.getNeighbours()) {
                MapProperties properties = other.getMapProperties();
                if (properties.isWater() && !properties.isOcean()) {
                    properties.setIsOcean(true);
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
                if (properties.isOcean()) oceans++;
                if (!properties.isWater()) lands++;
                if (oceans > 0 && lands > 0) {
                    break;
                }
            }
            center.getMapProperties().setIsCoast(oceans > 0 && lands > 0);
        }
    }

    private static void markCornerOceanProperties(List<Corner> corners) {
        for (Corner corner : corners) {
            int oceans = 0;
            int lands = 0;
            for (Center c : corner.getTouches()) {
                MapProperties properties = c.getMapProperties();
                if (properties.isOcean()) oceans++;
                if (!properties.isWater()) lands++;
                if (oceans > 0 && lands > 0) {
                    break;
                }
            }
            MapProperties properties = corner.getMapProperties();
            properties.setIsCoast(oceans > 0 && lands > 0);
            properties.setIsOcean(oceans == corner.getTouches().size());
            properties.setIsWater(corner.isWorldBorder()
                    || ((lands != corner.getTouches().size() && ! properties.isCoast())));
        }
    }

    private static List<MapPiece> createMapPieces(List<Center> centers) {
        List<MapPiece> map = new ArrayList<>(centers.size());
        for (Center center : centers) {
            MapProperties properties = center.getMapProperties();
            Color color = properties.isCoast() ? Color.YELLOW
                    : properties.isOcean() ? Color.BLUE
                    : properties.isWater() ? Color.CYAN
                    : Color.GREEN;
            map.add(new MapPiece(center, color));
        }
        return map;
    }

    private static VoronoiResults generateVoronoi(long seed, MutableBounds bounds) {
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
