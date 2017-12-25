package com.ninjarific.radiomesh.world.data;

public class MapProperties {

    private Type type = Type.LAND;

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

    public enum Type {
        BORDER_OCEAN,
        LAKE,
        COAST,
        SHALLOWS,
        LAND
    }
}
