package com.ninjarific.radiomesh.world.worldgenerator.biomes;

import com.ninjarific.radiomesh.world.data.MapProperties;

public class FullRangeBiomeMapper implements IBiomeMapper {

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
            } else if (moisture > 0.33) {
                return Biome.TUNDRA;
            } else if (moisture > 0.16) {
                return Biome.BARE;
            } else {
                return Biome.SCORCHED;
            }
        } else if (elevation > 0.6) {
            if (moisture > 0.66) {
                return Biome.TAIGA;
            } else if (moisture > 0.33) {
                return Biome.SHRUBLAND;
            } else {
                return Biome.TEMPERATE_DESERT;
            }
        } else if (elevation > 0.3) {
            if (moisture > 0.83) {
                return Biome.TEMPERATE_RAINFOREST;
            } else if (moisture > 0.5) {
                return Biome.TEMPERATE_FOREST;
            } else if (moisture > 0.16) {
                return Biome.GRASSLAND;
            } else {
                return Biome.TEMPERATE_DESERT;
            }
        } else {
            if (moisture > 0.66) {
                return Biome.TROPICAL_RAINFOREST;
            } else if (moisture > 0.33) {
                return Biome.TROPICAL_FOREST;
            } else if (moisture > 0.16) {
                return Biome.GRASSLAND;
            } else {
                return Biome.SUBTROPICAL_DESERT;
            }
        }
    }
}
