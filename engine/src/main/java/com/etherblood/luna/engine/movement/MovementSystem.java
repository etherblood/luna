package com.etherblood.luna.engine.movement;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.movement.collision.Body;
import com.etherblood.luna.engine.movement.collision.Collision;
import com.etherblood.luna.engine.movement.collision.CollisionEngine;
import com.etherblood.luna.engine.movement.collision.math.Fraction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MovementSystem implements GameSystem {
    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();

        Map<Integer, Vector2> speeds = new LinkedHashMap<>();

        for (int entity : data.list(Speed.class)) {
            Direction direction = data.get(entity, Direction.class);
            Vector2 speed = direction.toLengthVector(data.get(entity, Speed.class).milliMetresPerFrame());
            speeds.put(entity, speed);
        }

        List<Body> bodies = new ArrayList<>();
        for (int entity : data.list(Movebox.class)) {
            Body body = new Body();
            body.simulatedTime = Fraction.ofLong(0);
            body.speed = speeds.getOrDefault(entity, new Vector2(0, 0));
            body.setPosition(data.get(entity, Position.class).vector());
            body.id = entity;
            body.hitbox = data.get(entity, Movebox.class).shape();
            bodies.add(body);
        }
        List<Body> dynamicObstacles = new ArrayList<>();
        List<Body> staticObstacles = new ArrayList<>();
        for (int entity : data.list(Obstaclebox.class)) {
            Body body = bodies.stream().filter(other -> other.id == entity).findAny().orElse(null);
            if (body == null) {
                body = new Body();
                body.simulatedTime = Fraction.ofLong(0);
                body.speed = speeds.getOrDefault(entity, new Vector2(0, 0));
                body.setPosition(data.get(entity, Position.class).vector());
                body.id = entity;
                body.hitbox = data.get(entity, Obstaclebox.class).shape();
            }
            if (body.speed.equals(new Vector2(0, 0))) {
                staticObstacles.add(body);
            } else {
                dynamicObstacles.add(body);
            }
        }


        CollisionEngine collisionEngine = new CollisionEngine();
        Map<Integer, List<Collision>> collisions = collisionEngine.tick(bodies, body -> dynamicObstacles, body -> staticObstacles);


        for (Body body : bodies) {
            data.set(body.id, new Position(body.simulatedPositionX.floorToInt(), body.simulatedPositionY.floorToInt()));
        }
        for (int entity : data.list(Speed.class)) {
            if (data.has(entity, Movebox.class)) {
                continue;
            }
            Vector2 speed = speeds.get(entity);
            Position position = data.get(entity, Position.class);
            data.set(entity, new Position(position.vector().add(speed)));
        }
    }
}
