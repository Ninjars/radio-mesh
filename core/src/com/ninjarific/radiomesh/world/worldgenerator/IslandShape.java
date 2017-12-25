package com.ninjarific.radiomesh.world.worldgenerator;

import com.ninjarific.radiomesh.coordinates.Coordinate;

interface IslandShape {
    /**
     * @param x normalised from -1 to 1
     * @param y normalised from -1 to 1
     * @return if position is "on" the island shape
     */
    boolean isInside(double x, double y);
}
