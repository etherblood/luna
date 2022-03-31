package com.etherblood.luna.engine;

public record Circle(
        int x,
        int y,
        int radius
) {
    public Circle {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius " + radius + " is not positive.");
        }
    }

    public Circle translate(Vector2 position) {
        return translate(position.x(), position.y());
    }

    public Circle translate(int x, int y) {
        return new Circle(this.x + x, this.y + y, radius);
    }

    public boolean contains(Vector2 pos) {
        int dX = pos.x() - x;
        int dY = pos.y() - y;
        return dX * dX + dY * dY < radius * radius;
    }
}
