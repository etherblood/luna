package com.etherblood.luna.engine;

public record Direction(Vector2 kiloVector) {

    public static final int SQRT_HALF_MILLIS = 707;// Math.round(1000 * Math.sqrt(0.5))

    public static Direction UP = new Direction(new Vector2(0, 1000));
    public static Direction UP_RIGHT = new Direction(new Vector2(SQRT_HALF_MILLIS, SQRT_HALF_MILLIS));
    public static Direction RIGHT = new Direction(new Vector2(1000, 0));
    public static Direction DOWN_RIGHT = new Direction(new Vector2(SQRT_HALF_MILLIS, -SQRT_HALF_MILLIS));
    public static Direction DOWN = new Direction(new Vector2(0, -1000));
    public static Direction DOWN_LEFT = new Direction(new Vector2(-SQRT_HALF_MILLIS, -SQRT_HALF_MILLIS));
    public static Direction LEFT = new Direction(new Vector2(-1000, 0));
    public static Direction UP_LEFT = new Direction(new Vector2(-SQRT_HALF_MILLIS, SQRT_HALF_MILLIS));

    public Vector2 toLengthVector(long length) {
        return kiloVector.mult(length).floorDiv(1000);
    }

    public static Direction of(long x, long y) {
        if (x == 0 && y == 0) {
            return null;
        }
        long squaredLength = x * x + y * y;
        long length = (long) Math.sqrt(squaredLength);// TODO: use long math instead
        return new Direction(new Vector2(x * 1000, y * 1000).floorDiv(length));
    }

    public static Direction eightDirOf(long x, long y) {
        if (x == 0 && y == 0) {
            return null;
        }
        int milli_sin_of_pi_eights = 383;// Math.round(1000 * Math.sin(PI / 8))
        if (milli_sin_of_pi_eights * Math.abs(x) >= 1000 * Math.abs(y)) {
            if (x >= 0) {
                return Direction.RIGHT;
            } else {
                return Direction.LEFT;
            }
        }
        if (milli_sin_of_pi_eights * Math.abs(y) >= 1000 * Math.abs(x)) {
            if (y >= 0) {
                return Direction.UP;
            } else {
                return Direction.DOWN;
            }
        }
        if (x > 0) {
            if (y > 0) {
                return UP_RIGHT;
            }
            return DOWN_RIGHT;
        }
        if (y > 0) {
            return UP_LEFT;
        }
        return DOWN_LEFT;
    }
}
