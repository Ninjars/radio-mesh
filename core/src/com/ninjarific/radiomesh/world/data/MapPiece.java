package com.ninjarific.radiomesh.world.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;

import java.util.List;

public class MapPiece {

    private final Color color;
    private final Center center;

    public MapPiece(Center center, Color color) {
        this.color = color;
        this.center = center;
    }

    public List<Corner> getVertexes() {
        return center.getCorners();
    }

    public float width() {
        return center.getBounds().getWidth();
    }

    public float height() {
        return center.getBounds().getHeight();
    }

    public float getX() {
        return  center.getBounds().getX();
    }

    public float getY() {
        return  center.getBounds().getY();
    }

    public float getCenterX() {
        return (float) center.position.x;
    }

    public float getCenterY() {
        return (float) center.position.y;
    }

    public Color getColor() {
        return color;
    }

    public Polygon getPolygon() {
        return center.getPolygon();
    }
}
