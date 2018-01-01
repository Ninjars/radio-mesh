package com.ninjarific.radiomesh.world.data;

import com.ninjarific.radiomesh.coordinates.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Corner {
    public final int index;
    public Coordinate position;
    private final List<Center> touches = new ArrayList<>();
    private final List<Corner> adjacent = new ArrayList<>();
    private final List<Edge> protrudes = new ArrayList<>();

    private MapProperties mapProperties;
    private boolean isWorldBorder;
    private Corner downslope;
    private Corner watershed;
    private int watershedSize;

    public Corner(int index, Coordinate position) {
        this.index = index;
        this.position = position;
    }

    public boolean isWorldBorder() {
        return isWorldBorder;
    }

    public void setWorldBorder(boolean worldBorder) {
        isWorldBorder = worldBorder;
    }

    public List<Center> getTouches() {
        return touches;
    }

    public void addCenter(Center center) {
        if (center != null && !touches.contains(center)) {
            touches.add(center);
        }
    }

    public List<Edge> getProtrudes() {
        return protrudes;
    }

    public void addEdge(Edge edge) {
        protrudes.add(edge);
    }

    public List<Corner> getAdjacent() {
        return adjacent;
    }

    public void addAdjacent(Corner corner) {
        if (!adjacent.contains(corner)) {
            adjacent.add(corner);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Corner corner = (Corner) o;
        return Objects.equals(position, corner.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public void setMapProperties(MapProperties properties) {
        this.mapProperties = properties;
    }

    public MapProperties getMapProperties() {
        return mapProperties;
    }

    public void setDownslope(Corner downslope) {
        this.downslope = downslope;
    }

    public Corner getDownslope() {
        return downslope;
    }

    public void setWatershed(Corner watershed) {
        this.watershed = watershed;
    }

    public Corner getWatershed() {
        return watershed;
    }

    public int getWatershedSize() {
        return watershedSize;
    }

    public void setWatershedSize(int watershedSize) {
        this.watershedSize = watershedSize;
    }
}
