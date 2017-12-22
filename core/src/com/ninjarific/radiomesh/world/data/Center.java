package com.ninjarific.radiomesh.world.data;

import com.ninjarific.radiomesh.coordinates.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Center {
    private final int index;
    public final Coordinate position;

    public boolean isWorldBorder;
    private final List<Center> neighbours = new ArrayList<>();
    private final List<Edge> borders = new ArrayList<>();
    private final List<Corner> corners = new ArrayList<>();
    private final Comparator<Corner> cornerComparator;
    private MapProperties mapProperties;

    public Center(int index, Coordinate position) {
        this.index = index;
        this.position = position;
        cornerComparator = new CornerComparator(position);
    }

    public boolean isWorldBorder() {
        return isWorldBorder;
    }

    public void setWorldBorder(boolean worldBorder) {
        isWorldBorder = worldBorder;
    }

    public List<Center> getNeighbours() {
        return neighbours;
    }

    public void addNeighbour(Center neighbour) {
        if (!neighbours.contains(neighbour)) {
            neighbours.add(neighbour);
        }
    }

    public List<Edge> getBorders() {
        return borders;
    }

    public void addBorder(Edge border) {
        if (!borders.contains(border)) {
            borders.add(border);
            addCorner(border.v0);
            addCorner(border.v1);
        }
    }

    public List<Corner> getCorners() {
        return corners;
    }

    public void addCorner(Corner corner) {
        if (!corners.contains(corner)) {
            corners.add(corner);
            Collections.sort(corners, cornerComparator);
        }
    }

    public MapProperties getMapProperties() {
        return mapProperties;
    }

    public void setMapProperties(MapProperties mapProperties) {
        this.mapProperties = mapProperties;
    }

    private static class CornerComparator implements Comparator<Corner> {

        private final Coordinate center;

        public CornerComparator(Coordinate center) {

            this.center = center;
        }

        @Override
        public int compare(Corner a, Corner b) {
            return lessThan(a.position.x, a.position.y, b.position.x, b.position.y) ? 1 : -1;
        }

        private boolean lessThan(double ax, double ay, double bx, double by) {
            if (ax - center.x >= 0 && bx - center.x < 0) {
                return true;
            }
            if (ax - center.x < 0 && bx - center.x >= 0) {
                return false;
            }
            if (ax - center.x == 0 && bx - center.x == 0) {
                if (ay - center.y >= 0 || by - center.y >= 0) {
                    return ay > by;
                }
                return by > ay;
            }
            // compute the cross product of vectors (center -> a) x (center -> b)
            double det = (ax - center.x) * (by - center.y) - (bx - center.x) * (ay - center.y);
            if (det < 0)
                return true;
            if (det > 0)
                return false;

            // points a and b are on the same line from the center
            // check which point is closer to the center
            double d1 = (ax - center.x) * (ax - center.x) + (ay - center.y) * (ay - center.y);
            double d2 = (bx - center.x) * (bx - center.x) + (by - center.y) * (by - center.y);
            return d1 > d2;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Center center = (Center) o;
        return index == center.index &&
                Objects.equals(position, center.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, position);
    }
}
