package com.ninjarific.radiomesh.world.data;

public class MapProperties {
    private boolean isWater;
    private boolean isBorder;
    private boolean isOcean;
    private boolean isCoast;

    public boolean isWater() {
        return isWater;
    }

    public void setIsWater(boolean water) {
        isWater = water;
    }

    public void setIsBorder(boolean isBorder) {
        this.isBorder = isBorder;
    }

    public void setIsOcean(boolean isOcean) {
        this.isOcean = isOcean;
    }

    public boolean isOcean() {
        return isOcean;
    }

    public void setIsCoast(boolean isCoast) {
        this.isCoast = isCoast;
    }

    public boolean isCoast() {
        return isCoast;
    }
}
