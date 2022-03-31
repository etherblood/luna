package com.etherblood.luna.engine;

public record Rectangle(
        int x,
        int y,
        int width,
        int height
) {

    public Rectangle {
        if (width <= 0) {
            throw new IllegalArgumentException("width " + width + " is not positive.");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height " + height + " is not positive.");
        }
    }

    public int minX() {
        return x;
    }

    public int minY() {
        return y;
    }

    public int maxX() {
        return x + width;
    }

    public int maxY() {
        return y + height;
    }

    public Rectangle translate(Vector2 position) {
        return translate(position.x(), position.y());
    }

    public Rectangle translate(int x, int y) {
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

}
