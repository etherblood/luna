package com.etherblood.luna.engine.movement.collision;

import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.movement.collision.math.Fraction;

public record FractionRectangle(
        Fraction x, Fraction y,
        Fraction width, Fraction height
) {

    public static FractionRectangle ofHitbox(Rectangle hitbox) {
        return new FractionRectangle(
                Fraction.ofLong(hitbox.x()),
                Fraction.ofLong(hitbox.y()),
                Fraction.ofLong(hitbox.width()),
                Fraction.ofLong(hitbox.height())
        );
    }

    public Fraction minX() {
        return x;
    }

    public Fraction minY() {
        return y;
    }

    public Fraction maxX() {
        return x.add(width);
    }

    public Fraction maxY() {
        return y.add(height);
    }

    public FractionRectangle translate(Fraction x, Fraction y) {
        return new FractionRectangle(this.x.add(x), this.y.add(y), width, height);
    }
}
