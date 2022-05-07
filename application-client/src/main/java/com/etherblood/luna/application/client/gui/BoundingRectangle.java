package com.etherblood.luna.application.client.gui;

import org.joml.Vector2f;

public record BoundingRectangle(
        float x, float y, float width, float height
) {

    public boolean contains(Vector2f point) {
        return minX() <= point.x() && point.x() < maxX()
                && minY() <= point.y() && point.y() < maxY();
    }

    public float minX() {
        return x;
    }

    public float minY() {
        return y;
    }

    public float maxX() {
        return x + width;
    }

    public float maxY() {
        return y + height;
    }
}
