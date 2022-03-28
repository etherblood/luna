package com.etherblood.luna.engine;

public record Position(
        Vector2 vector
) {
    public Position(int x, int y) {
        this(new Vector2(x, y));
    }
}
