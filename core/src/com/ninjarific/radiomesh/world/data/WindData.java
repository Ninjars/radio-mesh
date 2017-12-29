package com.ninjarific.radiomesh.world.data;

public class WindData {
    private final Center center;

    public WindData(Center center) {
        this.center = center;
    }

    public double getDirection() {
        return center.getMapProperties().getWindDirection();
    }

    public float getX() {
        return (float) center.position.x;
    }

    public float getY() {
        return (float) center.position.y;
    }
}
