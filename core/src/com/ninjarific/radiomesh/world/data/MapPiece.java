package com.ninjarific.radiomesh.world.data;

import org.kynosarges.tektosyne.geometry.PointD;

public class MapPiece {

    private final PointD[] vertexes;
    private final float width;
    private final float height;
    private final float x;
    private final float y;
    private final float centerX;
    private final float centerY;

    public MapPiece(PointD[] region) {
        vertexes = region;

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        for (int i = 0; i < region.length; i++) {
            float x = (float) region[i].x;
            float y = (float) region[i].y;
            if (x > maxX) {
                maxX = x;
            }
            if (x < minX) {
                minX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (y < minY) {
                minY = y;
            }
        }

        width = maxX - minX;
        height = maxY - minY;

        this.x = minX;
        this.y = minY;
        this.centerX = minX + width / 2f;
        this.centerY = minY + height / 2f;
    }

    public PointD[] getVertexes() {
        return vertexes;
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
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }
}
