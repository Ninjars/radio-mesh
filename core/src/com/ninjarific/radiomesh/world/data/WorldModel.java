package com.ninjarific.radiomesh.world.data;

import com.ninjarific.radiomesh.scan.nodes.MutableBounds;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldModel {
    private static final int WORLD_SIZE = 100;
    private static final int POINT_COUNT = 500;
    private final MutableBounds bounds;
    private final List<MapPiece> map;

    public WorldModel() {
        VoronoiResults voronoiResults = generateVoronoi(getSeed(), new MutableBounds(0, 0, WORLD_SIZE, WORLD_SIZE));
        map = createMapPieces(voronoiResults);
        RectD clippingBounds = voronoiResults.clippingBounds;
        bounds = new MutableBounds(clippingBounds.min.x, clippingBounds.min.y, clippingBounds.max.x, clippingBounds.max.y);
    }

    private long getSeed() {
        return 1;
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

    private static List<MapPiece> createMapPieces(VoronoiResults voronoiResults) {
        PointD[][] regions = voronoiResults.voronoiRegions();
        List<MapPiece> map = new ArrayList<>(regions.length);
        for (PointD[] region : regions) {
            map.add(new MapPiece(region));
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


    public MutableBounds getBounds() {
        return bounds;
    }
}
