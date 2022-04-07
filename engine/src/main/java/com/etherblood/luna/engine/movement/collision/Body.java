package com.etherblood.luna.engine.movement.collision;


import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.movement.collision.math.Fraction;
import com.etherblood.luna.engine.movement.collision.math.Interval;

public class Body {

    public Integer id;
    public Fraction simulatedTime;
    public Fraction simulatedPositionX;
    public Fraction simulatedPositionY;
    public Vector2 speed;
    public Rectangle hitbox;

    public void setPosition(Vector2 position) {
        simulatedPositionX = Fraction.ofLong(position.x());
        simulatedPositionY = Fraction.ofLong(position.y());
    }

    public Rectangle moveBounds() {
        Fraction remainingTime = Fraction.ofLong(1).subtract(simulatedTime);
        long remainingOffsetX = remainingTime.multiply(Fraction.ofLong(speed.x())).upToInt();
        long x, width;
        if (remainingOffsetX > 0) {
            x = simulatedPositionX.floorToInt() + hitbox.x();
            width = hitbox.width() + remainingOffsetX;
        } else {
            x = simulatedPositionX.floorToInt() + hitbox.x() + remainingOffsetX;
            width = hitbox.width() - remainingOffsetX;
        }

        long remainingOffsetY = remainingTime.multiply(Fraction.ofLong(speed.y())).upToInt();
        long y, height;
        if (remainingOffsetY > 0) {
            y = simulatedPositionY.floorToInt() + hitbox.y();
            height = hitbox.height() + remainingOffsetY;
        } else {
            y = simulatedPositionY.floorToInt() + hitbox.y() + remainingOffsetY;
            height = hitbox.height() - remainingOffsetY;
        }
        return new Rectangle(x, y, width, height);
    }

    public IntersectionInfo intersect(Body other) {
        Fraction negativeSimulatedOffsetX = simulatedTime.multiply(Fraction.ofLong(-speed.x()));
        Fraction negativeSimulatedOffsetY = simulatedTime.multiply(Fraction.ofLong(-speed.y()));
        FractionRectangle a = FractionRectangle.ofHitbox(hitbox)
                .translate(
                        simulatedPositionX.add(negativeSimulatedOffsetX),
                        simulatedPositionY.add(negativeSimulatedOffsetY));

        Fraction otherNegativeSimulatedOffsetX = other.simulatedTime.multiply(Fraction.ofLong(-other.speed.x()));
        Fraction otherNegativeSimulatedOffsetY = other.simulatedTime.multiply(Fraction.ofLong(-other.speed.y()));
        FractionRectangle b = FractionRectangle.ofHitbox(other.hitbox)
                .translate(
                        other.simulatedPositionX.add(otherNegativeSimulatedOffsetX),
                        other.simulatedPositionY.add(otherNegativeSimulatedOffsetY));
        return intersect(a, speed, b, other.speed);
    }

    private static IntersectionInfo intersect(FractionRectangle a, Vector2 va, FractionRectangle b, Vector2 vb) {
        Vector2 v = va.sub(vb);
        Fraction vx = Fraction.ofLong(v.x());
        Fraction vy = Fraction.ofLong(v.y());

        Interval<Fraction> intersectX;
        if (v.x() != 0) {
            Fraction toiEnterX = b.minX().subtract(a.maxX()).divide(vx);
            Fraction toiLeaveX = b.maxX().subtract(a.minX()).divide(vx);
            intersectX = Interval.ofUnorderedBounds(toiEnterX, toiLeaveX);
        } else {
            if (a.maxX().compareTo(b.minX()) <= 0
                    || b.maxX().compareTo(a.minX()) <= 0) {
                return IntersectionInfo.noIntersection();
            }
            intersectX = new Interval(Fraction.negativeInfinity(), Fraction.positiveInfinity());
        }

        Interval<Fraction> intersectY;
        if (v.y() != 0) {
            Fraction toiEnterY = b.minY().subtract(a.maxY()).divide(vy);
            Fraction toiLeaveY = b.maxY().subtract(a.minY()).divide(vy);
            intersectY = Interval.ofUnorderedBounds(toiEnterY, toiLeaveY);
        } else {
            if (a.maxY().compareTo(b.minY()) <= 0
                    || b.maxY().compareTo(a.minY()) <= 0) {
                return IntersectionInfo.noIntersection();
            }
            intersectY = new Interval(Fraction.negativeInfinity(), Fraction.positiveInfinity());
        }

        Interval<Fraction> intersection = Interval.intersect(intersectX, intersectY);
        if (intersection == null) {
            return IntersectionInfo.noIntersection();
        }
        Fraction intersectStart = intersection.start();
        Fraction intersectXStart = intersectX.start();
        Fraction intersectYStart = intersectY.start();
        long x;
        long y;
        if (intersectStart.compareTo(intersectXStart) == 0) {
            x = v.x();
        } else {
            x = 0;
        }
        if (intersectStart.compareTo(intersectYStart) == 0) {
            y = v.y();
        } else {
            y = 0;
        }
        if (x == 0 && y == 0) {
            return IntersectionInfo.noIntersection();
        }

        return new IntersectionInfo(intersectStart, new CollisionDirection(x, y));
    }

    @Override
    public String toString() {
        return "Body{" +
                "id=" + id +
                '}';
    }
}
