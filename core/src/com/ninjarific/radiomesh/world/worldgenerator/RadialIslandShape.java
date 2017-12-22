package com.ninjarific.radiomesh.world.worldgenerator;

import com.ninjarific.radiomesh.coordinates.Coordinate;

import java.util.Random;

public class RadialIslandShape implements IslandShape {
    private static double ISLAND_FACTOR = 1.08; // 1.0 gives no small islands, 2.0 gives many

    private final int bumps;
    private final double startAngle;
    private final double dipAngle;
    private final double dipWidth;

    public RadialIslandShape(long seed) {
        Random random = new Random(seed);
        bumps = 1 + Math.round(random.nextFloat() * 5);
        startAngle = random.nextDouble() * 2 * Math.PI;
        dipAngle = random.nextDouble() * 2 * Math.PI;
        dipWidth = 0.2 + random.nextDouble() * 0.5;
    }

    @Override
    public boolean isInside(Coordinate coordinate) {
        double angle = Math.atan2(coordinate.y, coordinate.x);
        double length = 0.5 + (Math.max(Math.abs(coordinate.x), Math.abs(coordinate.y)) + coordinate.length());

        double r1 = 0.1 + 0.4 * Math.sin(startAngle + bumps * angle + Math.cos((bumps + 3) * angle));
        double r2 = 1.5 - 0.2 * Math.sin(startAngle + bumps * angle + Math.cos((bumps + 2) * angle));

        if (Math.abs(angle - dipAngle) < dipWidth
            || Math.abs(angle - dipAngle + 2 * Math.PI) < dipWidth
            || Math.abs(angle - dipAngle - 2 * Math.PI) < dipWidth) {
            r1 = r2 = 0.2;
        }
        return (length < r1
                || (length > r1 * ISLAND_FACTOR && length < r2));
    }
}
