package com.ninjarific.radiomesh.world.data;

import com.badlogic.gdx.graphics.Color;

import java.util.List;

public class MapPiece {

    private final Color color;
    private final float width;
    private final float height;
    private final float x;
    private final float y;
    private final Center center;

    public MapPiece(Center center, Color color) {
        this.color = color;
        this.center = center;

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        for (Corner corner : center.getCorners()) {
            if (corner.position.x < minX) {
                minX = (float) corner.position.x;
            }
            if (corner.position.x > maxX) {
                maxX = (float) corner.position.x;
            }
            if (corner.position.y < minY) {
                minY = (float) corner.position.y;
            }
            if (corner.position.y > maxY) {
                maxY = (float) corner.position.y;
            }
        }

        width = maxX - minX;
        height = maxY - minY;

        this.x = minX;
        this.y = minY;
    }

    public List<Corner> getVertexes() {
        return center.getCorners();
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float getX() {
        return  x;
    }

    public float getY() {
        return  y;
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

    public double getWindDirection() {
        return center.getMapProperties().getMoisture();
    }
}
