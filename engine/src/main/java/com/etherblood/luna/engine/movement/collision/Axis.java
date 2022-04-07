package com.etherblood.luna.engine.movement.collision;


import com.etherblood.luna.engine.Vector2;

public enum Axis {
    X, Y;

    public Vector2 set(Vector2 speed, long value) {
        if (this == X) {
            return new Vector2(value, speed.y());
        }
        return new Vector2(speed.x(), value);
    }

    public long get(Vector2 speed) {
        if (this == X) {
            return speed.x();
        }
        return speed.y();
    }
}
