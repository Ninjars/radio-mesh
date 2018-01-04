package com.ninjarific.radiomesh.world.worldgenerator.biomes;

import com.ninjarific.radiomesh.world.data.MapProperties;

public class TemperateBiomeMapper implements IBiomeMapper {

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
            } else {
                return Biome.TUNDRA;
            }
        } else if (elevation > 0.6) {
            if (moisture > 0.66) {
                return Biome.TAIGA;
            } else {
                return Biome.SHRUBLAND;
            }
        } else if (elevation > 0.3) {
            if (moisture > 0.7) {
                return Biome.TEMPERATE_FOREST;
            } else {
                return Biome.GRASSLAND;
            }
        } else {
            if (moisture > 0.45) {
                return Biome.TEMPERATE_FOREST;
            } else {
                return Biome.GRASSLAND;
            }
        }
    }
}
