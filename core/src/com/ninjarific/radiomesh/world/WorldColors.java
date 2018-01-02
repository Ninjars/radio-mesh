package com.ninjarific.radiomesh.world;

import com.badlogic.gdx.graphics.Color;
import com.ninjarific.radiomesh.world.worldgenerator.Biome;

import java.util.Random;

public class WorldColors {

    private static final Color COAST = new Color(.871f, 0.815f, 0.584f, 1f);
    private static final Color OCEAN = new Color(0.133f, 0.282f, 0.412f, 1f);
    private static final Color SHALLOWS = new Color(0.220f, 0.482f, 0.671f, 1f);
    private static final Color LAKE = new Color(0.255f, 0.584f, 0.820f, 1f);

    private static final Color SNOW = new Color(0.973f, 0.973f, 0.973f, 1f);
    private static final Color TUNDRA = new Color(0.867f, 0.867f, 0.733f, 1f);

    private static final Color BARE = new Color(0.733f, 0.733f, 0.733f, 1f);
    private static final Color SCORCHED = new Color(0.6f, 0.6f, 0.6f, 1f);

    private static final Color TAIGA = new Color(0.8f, 0.831f, 0.733f, 1f);
    private static final Color SHRUBLAND = new Color(0.769f, 0.8f, 0.733f, 1f);
    private static final Color TEMPERATE_DESERT = new Color(0.894f, 0.91f, 0.782f, 1f);

    private static final Color TEMPERATE_RAINFOREST = new Color(0.643f, 0.769f, 0.659f, 1f);
    private static final Color TEMPERATE_FOREST = new Color(0.706f, 0.788f, 0.663f, 1f);
    private static final Color GRASSLAND = new Color(0.769f, 0.831f, 0.667f, 1f);

    private static final Color TROPICAL_RAINFOREST = new Color(0.612f, 0.733f, 0.663f, 1f);
    private static final Color TROPICAL_FOREST = new Color(0.663f, 0.8f, 0.643f, 1f);
    private static final Color SUBTROPICAL_DESERT = new Color(0.914f, 0.867f, 0.78f, 1f);

    private static final Color UNASSIGNED_COLOR = Color.PURPLE;

    private static final float MARSH = 0.5f;
    private static final float ICE = 1.4f;

    public static Color getHeightMapColor(Random random, double elevation) {
        float val = random.nextFloat() * 0.025f + ((float) elevation * 0.975f);
        return new Color(val, val, val, 1f);
    }

    private static Color getFreshwaterColor(Random random, float multiplier) {
        Color color = new Color(LAKE);
        return color.mul(multiplier).mul(getColorMult(random), getColorMult(random), getColorMult(random), 1f);
    }

    private static Color randomiseColor(Random random, Color color) {
        return new Color(color).mul(getColorMult(random), getColorMult(random), getColorMult(random), 1f);
    }

    private static float getColorMult(Random random) {
        return 0.96f + 0.04f * random.nextFloat();
    }

    public static Color getGreyscale(float val) {
        return new Color(val, val, val, 1f);
    }

    public static Color getBiomeColor(Random colorRandom, Biome biome) {
        switch (biome) {
            case OCEAN:
                return randomiseColor(colorRandom, OCEAN);
            case COAST:
                return randomiseColor(colorRandom, COAST);
            case SHALLOWS:
                return randomiseColor(colorRandom, SHALLOWS);

            case LAKE:
                return WorldColors.getFreshwaterColor(colorRandom, 1);
            case MARSH:
                return WorldColors.getFreshwaterColor(colorRandom, MARSH);
            case ICE:
                return WorldColors.getFreshwaterColor(colorRandom, ICE);

            case SNOW:
                return randomiseColor(colorRandom, SNOW);
            case TUNDRA:
                return randomiseColor(colorRandom, TUNDRA);
            case BARE:
                return randomiseColor(colorRandom, BARE);
            case SCORCHED:
                return randomiseColor(colorRandom, SCORCHED);
            case TAIGA:
                return randomiseColor(colorRandom, TAIGA);
            case SHRUBLAND:
                return randomiseColor(colorRandom, SHRUBLAND);
            case TEMPERATE_DESERT:
                return randomiseColor(colorRandom, TEMPERATE_DESERT);
            case TEMPERATE_RAINFOREST:
                return randomiseColor(colorRandom, TEMPERATE_RAINFOREST);
            case TEMPERATE_FOREST:
                return randomiseColor(colorRandom, TEMPERATE_FOREST);
            case GRASSLAND:
                return randomiseColor(colorRandom, GRASSLAND);
            case TROPICAL_RAINFOREST:
                return randomiseColor(colorRandom, TROPICAL_RAINFOREST);
            case TROPICAL_FOREST:
                return randomiseColor(colorRandom, TROPICAL_FOREST);
            case SUBTROPICAL_DESERT:
                return randomiseColor(colorRandom, SUBTROPICAL_DESERT);
            default:
                return WorldColors.UNASSIGNED_COLOR;
        }
    }
}
