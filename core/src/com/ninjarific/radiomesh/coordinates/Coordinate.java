package com.ninjarific.radiomesh.coordinates;

import java.util.Objects;

public class Coordinate {
    public final double x;
    public final double y;
    private double length = -1;

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate midpoint(Coordinate a, Coordinate b) {
        return new Coordinate((a.x + b.x) / 2.0, (a.y + b.y) / 2.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public double length() {
        if (length < 0) {
            length = Math.sqrt(x * x + y * y);
        }
        return length;
    }
}
