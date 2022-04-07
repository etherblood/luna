package com.etherblood.luna.engine;

public record Vector2(
        long x,
        long y
) {

    public static Vector2 zero() {
        return new Vector2(0, 0);
    }

    public Vector2 add(long x, long y) {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 add(Vector2 v) {
        return add(v.x(), v.y());
    }

    public long squaredLength() {
        return x * x + y * y;
    }

    public long squaredDistance(Vector2 other) {
        return sub(other).squaredLength();
    }

    public Vector2 sub(Vector2 other) {
        return add(-other.x(), -other.y());
    }

    public Vector2 mult(long factor) {
        return new Vector2(factor * x, factor * y);
    }

    public Vector2 floorDiv(long denominator) {
        return new Vector2(Math.floorDiv(x, denominator), Math.floorDiv(y, denominator));
    }

}
