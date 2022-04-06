package com.etherblood.luna.engine;

public enum Direction {
    UP,
    UP_RIGHT,
    RIGHT,
    DOWN_RIGHT,
    DOWN,
    DOWN_LEFT,
    LEFT,
    UP_LEFT;

    public Vector2 toLengthVector(long length) {
        long diagonalSideLength = length * 707 / 1000;// length * Math.sqrt(0.5)
        return switch (this) {
            case UP -> new Vector2(0, length);
            case DOWN -> new Vector2(0, -length);
            case RIGHT -> new Vector2(length, 0);
            case LEFT -> new Vector2(-length, 0);
            case UP_RIGHT -> new Vector2(diagonalSideLength, diagonalSideLength);
            case DOWN_RIGHT -> new Vector2(diagonalSideLength, -diagonalSideLength);
            case UP_LEFT -> new Vector2(-diagonalSideLength, diagonalSideLength);
            case DOWN_LEFT -> new Vector2(-diagonalSideLength, -diagonalSideLength);
        };
    }

    public static Direction of(long x, long y) {
        if (x < 0) {
            if (y < 0) {
                return DOWN_LEFT;
            }
            if (y > 0) {
                return UP_LEFT;
            }
            return LEFT;
        }
        if (x > 0) {
            if (y < 0) {
                return DOWN_RIGHT;
            }
            if (y > 0) {
                return UP_RIGHT;
            }
            return RIGHT;
        }
        if (y < 0) {
            return DOWN;
        }
        if (y > 0) {
            return UP;
        }
        return null;
    }
}
