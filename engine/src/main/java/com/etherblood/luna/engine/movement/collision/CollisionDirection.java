package com.etherblood.luna.engine.movement.collision;

public record CollisionDirection(long x, long y) {

    public CollisionDirection(long x, long y) {
        this.x = Long.signum(x);
        this.y = Long.signum(y);
        if (x == 0 && y == 0) {
            throw new ArithmeticException("Direction cant have length 0.");
        }
    }

    public int priority() {
        if (x != 0 && y != 0) {
            return 2;
        }
        if (y != 0) {
            return 1;
        }
        return 0;
    }

    public Axis toAxis() {
        if (x != 0) {
            return Axis.X;
        }
        return Axis.Y;
    }
}
