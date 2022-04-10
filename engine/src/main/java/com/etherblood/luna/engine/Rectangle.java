package com.etherblood.luna.engine;

public record Rectangle(
        long x,
        long y,
        long width,
        long height
) {

    public Rectangle {
        if (width <= 0) {
            throw new IllegalArgumentException("width " + width + " is not positive.");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height " + height + " is not positive.");
        }
    }

    public long minX() {
        return x;
    }

    public long minY() {
        return y;
    }

    public long maxX() {
        return x + width;
    }

    public long maxY() {
        return y + height;
    }

    public Rectangle translate(Vector2 position) {
        return translate(position.x(), position.y());
    }

    public Rectangle translate(long x, long y) {
        return new Rectangle(this.x + x, this.y + y, width, height);
    }

    public boolean intersects(Rectangle other) {
        return minX() < other.maxX() && other.minX() < maxX()
                && minY() < other.maxY() && other.minY() < maxY();
    }

    public boolean contains(Vector2 pos) {
        return minX() < pos.x() && pos.x() < maxX()
                && minY() < pos.y() && pos.y() < maxY();
    }

    public boolean containsIncludeBounds(Vector2 pos) {
        return minX() <= pos.x() && pos.x() <= maxX()
                && minY() <= pos.y() && pos.y() <= maxY();
    }

}
