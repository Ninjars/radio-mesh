package com.ninjarific.radiomesh.world.worldgenerator;

import com.ninjarific.radiomesh.coordinates.Bounds;
import com.ninjarific.radiomesh.world.data.Center;
import com.ninjarific.radiomesh.world.data.Corner;
import com.ninjarific.radiomesh.world.data.Edge;
import com.ninjarific.radiomesh.world.data.MapPiece;
import com.ninjarific.radiomesh.world.data.WeatherData;

import java.util.List;

public class WorldModel {
    private final List<WeatherData> wind;
    private final Bounds bounds;
    private final List<MapPiece> map;

    private final List<Center> centers;
    private final List<Edge> edges;
    private final List<Corner> corners;

    public WorldModel(List<MapPiece> map, List<WeatherData> wind, Bounds bounds, List<Center> centers, List<Corner> corners, List<Edge> edges) {
        this.map = map;
        this.wind = wind;
        this.bounds = bounds;
        this.centers = centers;
        this.corners = corners;
        this.edges = edges;
    }

    public List<MapPiece> getMap() {
        return map;
    }

    public List<WeatherData> getWind() {
        return wind;
    }

    public Bounds getBounds() {
        return bounds;
    }
}
