package com.ninjarific.radiomesh.world.worldgenerator;

import com.ninjarific.radiomesh.coordinates.Coordinate;
import com.ninjarific.radiomesh.world.data.Center;
import com.ninjarific.radiomesh.world.data.Corner;
import com.ninjarific.radiomesh.world.data.Edge;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class WorldGeneratorUtils {
    private WorldGeneratorUtils() {}

    static List<Corner> createCorners(PointD[] cornerPoints, RectD bounds) {
        List<Corner> corners = new ArrayList(cornerPoints.length);

        for(int i = 0; i < cornerPoints.length; ++i) {
            PointD position = cornerPoints[i];
            corners.add(new Corner(i, offsetCoordinate(position, bounds)));
        }

        return corners;
    }

    static List<Center> createCenters(PointD[] centerPoints, RectD bounds) {
        List<Center> centers = new ArrayList(centerPoints.length);

        for(int i = 0; i < centerPoints.length; ++i) {
            PointD position = centerPoints[i];
            centers.add(new Center(i, offsetCoordinate(position, bounds)));
        }

        return centers;
    }

    private static Coordinate offsetCoordinate(PointD position, RectD bounds) {
        return new Coordinate((position.x - bounds.min.x), (position.y - bounds.min.y));
    }

    static List<Edge> createEdges(VoronoiEdge[] voronoiEdges, List<Center> centers, List<Corner> corners) {
        List<Edge> edges = new ArrayList();

        for(int i = 0; i < voronoiEdges.length; ++i) {
            VoronoiEdge resultEdge = voronoiEdges[i];
            Center centerA = centers.get(resultEdge.site1);
            Center centerB = centers.get(resultEdge.site2);
            Corner cornerA = corners.get(resultEdge.vertex1);
            Corner cornerB = corners.get(resultEdge.vertex2);
            edges.add(makeEdge(edges.size(), cornerA, cornerB, centerA, centerB));
        }

        return edges;
    }

    private static Edge makeEdge(int index, Corner cornerA, Corner cornerB, Center a, Center b) {
        Edge edge = new Edge(index, a, b, cornerA, cornerB);
        edge.v0.addEdge(edge);
        edge.v1.addEdge(edge);
        if(edge.d0 != null && edge.d1 != null) {
            edge.d0.addNeighbour(edge.d1);
            edge.d1.addNeighbour(edge.d0);
        }

        edge.v0.addAdjacent(edge.v1);
        edge.v1.addAdjacent(edge.v0);
        edge.v0.addCenter(edge.d0);
        edge.v0.addCenter(edge.d1);
        edge.v1.addCenter(edge.d0);
        edge.v1.addCenter(edge.d1);
        return edge;
    }

    static void improveCorners(List<Corner> corners) {
        Iterator var1 = corners.iterator();

        while(true) {
            Corner corner;
            do {
                if(!var1.hasNext()) {
                    return;
                }

                corner = (Corner)var1.next();
                corner.setWorldBorder(corner.getTouches().size() <= 2);
            } while(corner.isWorldBorder());

            double x = 0.0D;
            double y = 0.0D;

            Center center;
            for(Iterator var7 = corner.getTouches().iterator(); var7.hasNext(); y += center.position.y) {
                center = (Center)var7.next();
                x += center.position.x;
            }

            x /= (double)corner.getTouches().size();
            y /= (double)corner.getTouches().size();
            corner.setPosition(new Coordinate(x, y));
        }
    }
}
