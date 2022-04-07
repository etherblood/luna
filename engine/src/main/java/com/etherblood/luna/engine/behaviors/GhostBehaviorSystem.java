package com.etherblood.luna.engine.behaviors;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorInput;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.ActionKey;
import com.etherblood.luna.engine.damage.MilliHealth;
import com.etherblood.luna.engine.damage.Team;

public class GhostBehaviorSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(GhostBehavior.class)) {
            Integer best = findNearestEnemy(data, entity);
            if (best != null) {
                Position position = data.get(entity, Position.class);
                Position otherPosition = data.get(best, Position.class);
                Vector2 delta = otherPosition.vector().sub(position.vector());

                // <hacky workaround>
                Direction direction;
                int milli_sin_of_pi_eights = 383;// Math.round(1000 * Math.sin(PI / 8))
                if (milli_sin_of_pi_eights * Math.abs(delta.x()) >= 1000 * Math.abs(delta.y())) {
                    if (delta.x() >= 0) {
                        direction = Direction.RIGHT;
                    } else {
                        direction = Direction.LEFT;
                    }
                } else if (milli_sin_of_pi_eights * Math.abs(delta.y()) >= 1000 * Math.abs(delta.x())) {
                    if (delta.y() >= 0) {
                        direction = Direction.UP;
                    } else {
                        direction = Direction.DOWN;
                    }
                } else {
                    direction = Direction.of(delta.x(), delta.y());
                }
                // </hacky workaround>

                int attackRange = 1000;
                if (delta.squaredLength() < attackRange * attackRange) {
                    data.set(entity, new ActorInput(direction, ActionKey.ATTACK1));
                } else {
                    data.set(entity, new ActorInput(direction, ActionKey.WALK));
                }
            }
        }
    }

    private static Integer findNearestEnemy(EntityData data, int entity) {
        Team team = data.get(entity, Team.class);
        Position position = data.get(entity, Position.class);

        Integer best = null;
        long bestSquaredDistance = Integer.MAX_VALUE;

        for (int other : data.list(Position.class)) {
            Team otherTeam = data.get(other, Team.class);
            if (team != null && team.equals(otherTeam)) {
                continue;
            }
            Position otherPosition = data.get(other, Position.class);
            MilliHealth otherHealth = data.get(other, MilliHealth.class);
            if (otherPosition == null || otherHealth == null || otherHealth.value() <= 0) {
                continue;
            }
            long squaredDistance = position.vector().squaredDistance(otherPosition.vector());
            if (squaredDistance < bestSquaredDistance) {
                bestSquaredDistance = squaredDistance;
                best = other;
            }
        }
        return best;
    }
}
