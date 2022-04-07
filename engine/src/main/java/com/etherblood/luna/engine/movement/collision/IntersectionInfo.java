package com.etherblood.luna.engine.movement.collision;


import com.etherblood.luna.engine.movement.collision.math.Fraction;

public record IntersectionInfo(Fraction time, CollisionDirection normal) {

    public static IntersectionInfo noIntersection() {
        return new IntersectionInfo(null, null);
    }

    public boolean isIntersecting() {
        return time != null
                && time.compareTo(Fraction.ofLong(0)) >= 0
                && time.compareTo(Fraction.ofLong(1)) < 0;
    }
}
