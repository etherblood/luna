package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class MovementSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(Speed.class)) {
            Direction direction = data.get(entity, Direction.class);
            Vector2 speed = direction.toLengthVector(data.get(entity, Speed.class).milliMetresPerFrame());
            Position position = data.get(entity, Position.class);
            Movebox movebox = data.get(entity, Movebox.class);
            if (movebox != null) {
                Rectangle moveboxShape = movebox.shape().translate(position.vector());

                Vector2 min = new Vector2(
                        moveboxShape.minX() + Math.min(0, speed.x()),
                        moveboxShape.minY() + Math.min(0, speed.y()));
                Vector2 max = new Vector2(
                        moveboxShape.maxX() + Math.max(0, speed.x()),
                        moveboxShape.maxY() + Math.max(0, speed.y()));

                //TODO: collisions
            }
            data.set(entity, new Position(position.vector().add(speed)));
        }
    }
}
