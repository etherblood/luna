package com.etherblood.luna.engine.behaviors;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorInput;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.ActionKey;
import com.etherblood.luna.engine.actions.Attack1Cooldown;
import com.etherblood.luna.engine.actions.Attack2Cooldown;
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
                Direction direction = Direction.of(delta.x(), delta.y());

                int meleeRange = 1000;
                int rangedRange = 3000;
                if (!data.has(entity, Attack1Cooldown.class) && delta.squaredLength() < meleeRange * meleeRange) {
                    data.set(entity, new ActorInput(direction, ActionKey.ATTACK1));
                } else if (!data.has(entity, Attack2Cooldown.class) && delta.squaredLength() < rangedRange * rangedRange) {
                    data.set(entity, new ActorInput(direction, ActionKey.ATTACK2));
                } else {
                    data.set(entity, new ActorInput(direction, ActionKey.WALK));
                }
            } else {
                data.set(entity, new ActorInput(null, ActionKey.IDLE));
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
