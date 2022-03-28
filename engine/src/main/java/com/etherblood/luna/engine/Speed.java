package com.etherblood.luna.engine;

public record Speed(
        Vector2 vector
) {
    public Speed(int x, int y) {
        this(new Vector2(x, y));
    }
}
