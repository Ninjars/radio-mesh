package com.ninjarific.radiomesh.world.data;

public class MapProperties {

    private Type type = Type.LAND;
    private double elevation;
    private double windDirection;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isLand() {
        return type == Type.COAST || type == Type.LAND;
    }

    public boolean isOcean() {
        return type == Type.SHALLOWS || type == Type.BORDER_OCEAN;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getElevation() {
        return elevation;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public enum Type {
        BORDER_OCEAN,
        LAKE,
        COAST,
        SHALLOWS,
        LAND
    }
}
