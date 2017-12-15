package com.ninjarific.radiomesh.world.data;

import com.badlogic.gdx.graphics.Color;
import com.ninjarific.radiomesh.coordinates.Bounds;
import com.ninjarific.radiomesh.coordinates.Coordinate;
import com.ninjarific.radiomesh.coordinates.MutableBounds;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldModel {
    private static final int WORLD_SIZE = 100;
    private static final int POINT_COUNT = 500;
    private final Bounds bounds;
    private final List<MapPiece> map;
    private final NodeData nodeData;

    private final List<Center> centers;
    private final List<Edge> edges;
    private final List<Corner> corners;

    public WorldModel(NodeData nodeData) {
        this.nodeData = nodeData;
        VoronoiResults voronoiResults = generateVoronoi(getSeed(), new MutableBounds(0, 0, WORLD_SIZE, WORLD_SIZE));

        PointD[] centerPoints = voronoiResults.generatorSites;
        centers = new ArrayList<>(centerPoints.length);
        for (int i = 0; i < centerPoints.length; i++) {
            PointD position = centerPoints[i];
            Center center = new Center(i, new Coordinate(position.x, position.y));
            centers.add(center);
        }

        edges = new ArrayList<>();
        corners = new ArrayList<>();

        RectD clippingBoundRect = voronoiResults.clippingBounds;
        bounds = new Bounds(clippingBoundRect.min.x, clippingBoundRect.min.y, clippingBoundRect.max.x, clippingBoundRect.max.y);

        PointD[][] regions = voronoiResults.voronoiRegions();
        for (int i = 0; i < regions.length; i++) {
            PointD[] region = regions[i];
            for (int v = 0; v < region.length; v++) {
                PointD vertex1 = region[v];
                PointD vertex2 = v+1 == region.length ? region[0] : region[v+1];
                Center centerA = centers.get(i);
                Center centerB = lookupOtherCenter(voronoiResults, centers, centerA, vertex1, vertex2);
                edges.add(makeEdge(bounds, edges.size(), corners, vertex1, vertex2, centers.get(i), centerB));
            }
        }

        improveCorners();

        map = createMapPieces(getSeed(), centers);
    }

    private void improveCorners() {
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

    private static Center lookupOtherCenter(VoronoiResults results, List<Center> centers, Center centerA, PointD vertex1, PointD vertex2) {
        for (VoronoiEdge edge : results.voronoiEdges) {
            PointD va = results.voronoiVertices[edge.vertex1];
            PointD vb = results.voronoiVertices[edge.vertex2];
            if ((va == vertex1 && vb == vertex2) || (vb == vertex1 && va == vertex2)) {
                // we have found an edge which matches the edge we're searching on
                PointD site1 = results.generatorSites[edge.site1];
                PointD site2 = results.generatorSites[edge.site2];
                if (doesPositionMatch(site1, centerA.position)) {
                    return centers.get(edge.site2);
                } else if (doesPositionMatch(site2, centerA.position)) {
                    return centers.get(edge.site1);
                }
            }
        }
        return null;
    }

    private static boolean doesPositionMatch(PointD point, Coordinate coordinate) {
        return Double.compare(point.x, coordinate.x) == 0 && Double.compare(point.y, coordinate.y) == 0;
    }

    private static Edge makeEdge(Bounds bounds, int index, List<Corner> corners, PointD vertex1, PointD vertex2, Center a, Center b) {
        Corner cornerA = makeCorner(bounds, corners, vertex1);
        Corner cornerB = makeCorner(bounds, corners, vertex2);

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

    private static Corner makeCorner(Bounds bounds, List<Corner> corners, PointD vertex) {
        Corner corner = new Corner(corners.size(), new Coordinate(vertex.x, vertex.y));
        int existingIndex = corners.indexOf(corner);
        if (existingIndex >= 0) {
            return corners.get(existingIndex);
        } else {
            corner.setWorldBorder(vertex.x == bounds.left
                    || vertex.x == bounds.right
                    || vertex.y == bounds.top
                    || vertex.y == bounds.bottom);
            corners.add(corner);
            return corner;
        }
    }

    private long getSeed() {
        return nodeData.getSeed();
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

    private static List<MapPiece> createMapPieces(long seed, List<Center> centers) {
        List<MapPiece> map = new ArrayList<>(centers.size());
        Random random = new Random(seed);
        float baseR = random.nextFloat() * 0.3f + 0.1f;
        float baseG = random.nextFloat() * 0.3f + 0.1f;
        float baseB = random.nextFloat() * 0.3f + 0.1f;
        for (Center center : centers) {
            float saturation = random.nextFloat() * 0.3f;
            Color color = new Color(
                    baseR + saturation + 0.1f * random.nextFloat(),
                    baseG + saturation + 0.1f * random.nextFloat(),
                    baseB + saturation + 0.1f * random.nextFloat(),
                    1);
            map.add(new MapPiece(center, color));
        }
        return map;
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

    public List<MapPiece> getMap() {
        return map;
    }


    public Bounds getBounds() {
        return bounds;
    }
}
