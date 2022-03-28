package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class MovementSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(Speed.class)) {
            Vector2 speed = data.get(entity, Speed.class).vector();
            Position position = data.get(entity, Position.class);
            Rectangle movebox = data.get(entity, Movebox.class).rectangle().translate(position.vector());

            Vector2 min = new Vector2(
                    movebox.minX() + Math.min(0, speed.x()),
                    movebox.minY() + Math.min(0, speed.y()));
            Vector2 max = new Vector2(
                    movebox.maxX() + Math.max(0, speed.x()),
                    movebox.maxY() + Math.max(0, speed.y()));

            //TODO: collisions
            data.set(entity, new Position(position.vector().add(speed)));
            if (speed.x() != 0 || speed.y() != 0) {
                data.set(entity, Direction.of(speed.x(), speed.y()));
            }
        }
    }
}
