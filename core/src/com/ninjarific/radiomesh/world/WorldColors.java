package com.ninjarific.radiomesh.world;

import com.badlogic.gdx.graphics.Color;

import java.util.Random;

public class WorldColors {

    public static final Color LAND_COLOR_BASE = new Color(0.385f, 0.584f, 0.074f, 1f);
    public static final Color LAND_COLOR_PEAK = new Color(0.941f, 1f, 0.858f, 1f);
    public static final Color COAST_BASE = new Color(0.792f, 0.815f, 0.286f, 1f);
    public static final Color OCEAN_COLOR_BASE = new Color(0.133f, 0.282f, 0.412f, 1f);
    public static final Color SHALLOWS_BASE = new Color(0.361f, 0.553f, 0.718f, 1f);
    public static final Color LAKE_BASE = new Color(0.153f, 0.467f, 0.733f, 1f);
    public static final Color UNASSIGNED_COLOR = Color.PURPLE;

    public static Color getHeightMapColor(Random random, double elevation) {
        float val = random.nextFloat() * 0.025f + ((float) elevation * 0.975f);
        return new Color(val, val, val, 1f);
    }

    public static Color getLandColor(Random random, double elevation) {
        Color color = new Color(LAND_COLOR_BASE);
        color.lerp(LAND_COLOR_PEAK, (float) elevation);
        return color.mul(getColorMult(random), getColorMult(random), getColorMult(random), 1f);
    }

    public static Color getCoastColor(Random random) {
        Color color = new Color(COAST_BASE);
        return color.mul(getColorMult(random), getColorMult(random), getColorMult(random), 1f);
    }

    public static Color getOceanColor(Random random) {
        Color color = new Color(OCEAN_COLOR_BASE);
        return color.mul(getColorMult(random), getColorMult(random), getColorMult(random), 1f);
    }

    public static Color getShallowsColor(Random random) {
        Color color = new Color(SHALLOWS_BASE);
        return color.mul(getColorMult(random), getColorMult(random), getColorMult(random), 1f);
    }

    public static Color getLakeColor(Random random) {
        Color color = new Color(LAKE_BASE);
        return color.mul(getColorMult(random), getColorMult(random), getColorMult(random), 1f);
    }

    private static float getColorMult(Random random) {
        return 0.95f + 0.05f * random.nextFloat();
    }
}
