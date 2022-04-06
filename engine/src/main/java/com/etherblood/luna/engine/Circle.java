package com.etherblood.luna.engine;

public record Circle(
        long x,
        long y,
        long radius
) {
    public Circle {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius " + radius + " is not positive.");
        }
    }

    public Circle translate(Vector2 position) {
        return translate(position.x(), position.y());
    }

    public Circle translate(long x, long y) {
        return new Circle(this.x + x, this.y + y, radius);
    }

    public boolean contains(Vector2 pos) {
        long dX = pos.x() - x;
        long dY = pos.y() - y;
        return dX * dX + dY * dY < radius * radius;
    }

    public boolean intersects(Circle other) {
        long dX = other.x() - x;
        long dY = other.y() - y;
        long radiusSum = other.radius() + radius;
        return dX * dX + dY * dY < radiusSum * radiusSum;
    }
}
