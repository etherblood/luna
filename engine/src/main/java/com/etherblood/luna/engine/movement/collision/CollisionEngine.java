package com.etherblood.luna.engine.movement.collision;

import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.movement.collision.math.Fraction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CollisionEngine {

    public static final int COLLISION_LIMIT = 10;

    public Map<Integer, List<Collision>> tick(List<Body> bodies,
                                              Function<Rectangle, List<Body>> findKinematicCandidates,
                                              Function<Rectangle, List<Body>> findStaticCandidates) {
        Map<Integer, List<Collision>> result = new HashMap<>();
        while (true) {
            // TODO: broad phase, split bodies into multiple smaller groups which are isolated from each other
            List<Collision> intersections = new ArrayList<>();
            for (Body body : bodies) {
                // TODO: reuse lists from previous iteration when oldBounds.contains(newBounds)?
                Rectangle bounds = body.moveBounds();
                List<Body> kinematics = findKinematicCandidates.apply(bounds);
                List<Body> statics = findStaticCandidates.apply(bounds);

                List<Body> obstacles = new ArrayList<>();
                obstacles.addAll(kinematics);
                obstacles.addAll(statics);

                for (Body obstacle : obstacles) {
                    if (Objects.equals(body.id, obstacle.id)) {
                        continue;
                    }
                    IntersectionInfo info = body.intersect(obstacle);
                    if (info.isIntersecting()
                            && info.time().compareTo(body.simulatedTime) >= 0
                            && info.time().compareTo(obstacle.simulatedTime) >= 0) {

                        // make sure to handle kinematic intersections only once
                        // this might cause objects to clip through kinematics, but they will never clip statics
                        if (kinematics.contains(obstacle)
                                && result.getOrDefault(body.id, Collections.emptyList()).stream()
                                .filter(collision -> collision.b() == obstacle)
                                .count() >= COLLISION_LIMIT) {
                            continue;
                        }

                        intersections.add(new Collision(body, obstacle, info.time(), info.normal()));
                    }
                }
            }
            if (intersections.isEmpty()) {
                break;
            }
            Fraction timeOfIntersection = intersections.stream().min(Comparator.comparing(Collision::timeOfIntersection)).get().timeOfIntersection();
            List<Collision> collisions = intersections.stream()
                    .filter(collision -> collision.timeOfIntersection().compareTo(timeOfIntersection) == 0)
                    .sorted(Comparator.comparingInt(collision -> collision.normal().priority()))

                    //TODO: better workaround for multiple collisions with different axes?
                    .limit(1)
                    .toList();
            for (Collision info : collisions) {
                Body body = info.a();
                Fraction deltaTime = info.timeOfIntersection().subtract(body.simulatedTime);
                body.simulatedPositionX = body.simulatedPositionX.add(deltaTime.multiply(Fraction.ofLong(body.speed.x()))).reduce();
                body.simulatedPositionY = body.simulatedPositionY.add(deltaTime.multiply(Fraction.ofLong(body.speed.y()))).reduce();
                body.simulatedTime = info.timeOfIntersection().reduce();
                Axis axis = info.normal().toAxis();
                Vector2 nextSpeed = axis.set(body.speed, axis.get(info.b().speed));
                body.speed = nextSpeed;

                // TODO: result should also contain info about positions & speeds at timeOfIntersection?
                result.computeIfAbsent(body.id, x -> new ArrayList<>()).add(info);
            }
        }
        for (Body body : bodies) {
            Fraction deltaTime = Fraction.ofLong(1).subtract(body.simulatedTime);
            body.simulatedPositionX = body.simulatedPositionX.add(deltaTime.multiply(Fraction.ofLong(body.speed.x())));
            body.simulatedPositionY = body.simulatedPositionY.add(deltaTime.multiply(Fraction.ofLong(body.speed.y())));
            body.simulatedTime = Fraction.ofLong(1);
        }
        return result;
    }

}
