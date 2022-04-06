package com.etherblood.luna.engine;

public record Position(
        Vector2 vector
) {
    public Position(long x, long y) {
        this(new Vector2(x, y));
    }
}
