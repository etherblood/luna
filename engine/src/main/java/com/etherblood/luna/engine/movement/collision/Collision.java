package com.etherblood.luna.engine.movement.collision;

import com.etherblood.luna.engine.movement.collision.math.Fraction;

public record Collision(
        Body a,
        Body b,
        Fraction timeOfIntersection,
        CollisionDirection normal) {

}
