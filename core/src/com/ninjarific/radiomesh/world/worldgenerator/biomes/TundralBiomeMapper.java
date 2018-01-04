package com.ninjarific.radiomesh.world.worldgenerator.biomes;

import com.ninjarific.radiomesh.world.data.MapProperties;

public class TundralBiomeMapper implements IBiomeMapper {

    public Biome getBiome(MapProperties mapProperties) {
        double elevation = mapProperties.getElevation();
        double moisture = mapProperties.getMoisture();
        switch (mapProperties.getType()) {
            case BORDER_OCEAN:
                return Biome.OCEAN;
            case SHALLOWS:
                return Biome.SHALLOWS;
            case COAST:
                return Biome.COAST;
            case LAKE:
                if (elevation < 0.1) {
                    return Biome.MARSH;
                }
                if (elevation > 0.8) {
                    return Biome.ICE;
                }
                return Biome.LAKE;
        }
        if (elevation > 0.8) {
            if (moisture > 0.5) {
                return Biome.SNOW;
            } else if (moisture > 0.22) {
                return Biome.TUNDRA;
            } else {
                return Biome.BARE;
            }
        } else if (elevation > 0.6) {
            if (moisture > 0.4) {
                return Biome.TAIGA;
            } else if (moisture > 0.16) {
                return Biome.SHRUBLAND;
            } else {
                return Biome.TUNDRA;
            }
        } else if (elevation > 0.3) {
            if (moisture > 0.83) {
                return Biome.TAIGA;
            } else if (moisture > 0.5) {
                return Biome.TEMPERATE_FOREST;
            } else if (moisture > 0.16) {
                return Biome.GRASSLAND;
            } else {
                return Biome.TUNDRA;
            }
        } else {
            if (moisture > 0.5) {
                return Biome.TEMPERATE_FOREST;
            } else {
                return Biome.GRASSLAND;
            }
        }
    }
}
