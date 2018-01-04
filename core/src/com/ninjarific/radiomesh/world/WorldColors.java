package com.ninjarific.radiomesh.world;

import com.badlogic.gdx.graphics.Color;
import com.ninjarific.radiomesh.world.worldgenerator.biomes.Biome;

import java.util.Random;

public class WorldColors {

    private static final Color COAST = new Color(.871f, 0.815f, 0.584f, 1f);
    private static final Color OCEAN = new Color(0.133f, 0.282f, 0.412f, 1f);
    private static final Color SHALLOWS = new Color(0.220f, 0.482f, 0.671f, 1f);
    private static final Color LAKE = new Color(0.255f, 0.584f, 0.820f, 1f);

    private static final Color SNOW = new Color(0.973f, 0.973f, 0.973f, 1f);
    private static final Color TUNDRA = new Color(0.867f, 0.867f, 0.733f, 1f);

    private static final Color BARE = new Color(176/255f, 176/255f, 176/255f, 1f);
    private static final Color SCORCHED = new Color(145/255f, 145/255f, 145/255f, 1f);

    private static final Color TAIGA = new Color(191/255f, 219/255f, 196/255f, 1f);
    private static final Color SHRUBLAND = new Color(174/255f, 191/255f, 168/255f, 1f);
    private static final Color TEMPERATE_DESERT = new Color(208/255f, 222/255f, 138/255f, 1f);

    private static final Color TEMPERATE_RAINFOREST = new Color(131/255f, 209/255f, 123/255f, 1f);
    private static final Color TEMPERATE_FOREST = new Color(97/255f, 209/255f, 77/255f, 1f);
    private static final Color GRASSLAND = new Color(120/255f, 222/255f, 111/255f, 1f);

    private static final Color TROPICAL_RAINFOREST = new Color(78/255f, 179/255f, 64/255f, 1f);
    private static final Color TROPICAL_FOREST = new Color(74/255f, 168/255f, 30/255f, 1f);
    private static final Color SUBTROPICAL_DESERT = new Color(227/255f, 217/255f, 84/255f, 1f);

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
