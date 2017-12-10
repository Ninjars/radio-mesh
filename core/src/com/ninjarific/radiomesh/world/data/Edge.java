package com.ninjarific.radiomesh.world.data;

import com.ninjarific.radiomesh.coordinates.Coordinate;

public class Edge {
    public final int index;
    public final Center d0; // Delaunay edge
    public final Center d1; // Delaunay edge
    public final Corner v0; // Voronoi edge
    public final Corner v1; // Voronoi edge
    public final Coordinate midpoint; // Voronoi edge midpoint

    public Edge(int index, Center d0, Center d1, Corner v0, Corner v1) {
        this.index = index;
        this.d0 = d0;
        this.d1 = d1;
        this.v0 = v0;
        this.v1 = v1;
        midpoint = Coordinate.midpoint(v0.position, v1.position);

        if (d0 != null) {
            d0.addBorder(this);
        }
        if (d1 != null) {
            d1.addBorder(this);
        }
    }
}
