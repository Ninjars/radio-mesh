package com.ninjarific.radiomesh.world.worldgenerator;

import java.util.Random;

public class PerlinIslandShape implements IslandShape {
    private final double[] perlin;
    private final int size;


    PerlinIslandShape(long seed, int size) {
        this.size = size;
        Random random = new Random(seed);
        perlin = Noise.normalize(Noise.perlinNoise(random, size, size, 7));
    }

    @Override
    public boolean isInside(double x, double y) {
        int perlinX = (int) ((x + 1) * size / 2);
        int perlinY = (int) ((y + 1) * size / 2) * size;
        double perlinVal = perlin[perlinX + perlinY];
        double lineLength = Math.sqrt(x * x + y * y);
        return perlinVal > 0.3 + 0.7 * lineLength * lineLength;
    }
}
