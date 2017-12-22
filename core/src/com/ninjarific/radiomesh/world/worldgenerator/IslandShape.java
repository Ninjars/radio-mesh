package com.ninjarific.radiomesh.world.worldgenerator;

import com.ninjarific.radiomesh.coordinates.Coordinate;

interface IslandShape {
    boolean isInside(Coordinate coordinate);
}
