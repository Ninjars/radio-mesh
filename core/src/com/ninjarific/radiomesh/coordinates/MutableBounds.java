package com.ninjarific.radiomesh.coordinates;

public class MutableBounds {
    public double left;
    public double top;
    public double right;
    public double bottom;

    public MutableBounds() {}

    public MutableBounds(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public double getWidth() {
        return right - left;
    }

    public double getHeight() {
        return bottom - top;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

    public boolean contains(float x, float y) {
        return left <= x && x <= right && top <= y && y <= bottom;
    }

    public double centerX() {
        return left + (getWidth() / 2.0);
    }

    public double centerY() {
        return top + (getHeight() / 2.0);
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public void setRight(double right) {
        this.right = right;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public void set(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public String toString() {
        return "<MutableBounds " + left + ", " + top + " -> " + right + ", " + bottom + ">";
    }
}
