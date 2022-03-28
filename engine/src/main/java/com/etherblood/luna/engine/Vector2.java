package com.etherblood.luna.engine;

public record Vector2(
        int x,
        int y
) {

    public static Vector2 zero() {
        return new Vector2(0, 0);
    }

    public Vector2 add(int x, int y) {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 add(Vector2 v) {
        return add(v.x(), v.y());
    }
}
