package com.etherblood.luna.engine.behaviors;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActiveAction;
import com.etherblood.luna.engine.ActorInput;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Team;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.data.ActionEvent;
import com.etherblood.luna.engine.actions.data.ActionKey;
import com.etherblood.luna.engine.actions.data.ActionOf;
import com.etherblood.luna.engine.actions.data.ActiveCooldown;
import com.etherblood.luna.engine.damage.MilliHealth;

public class SimpleBehaviorSystem implements GameSystem {
    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int entity : data.list(SimpleBehavior.class)) {
            ActiveAction activeAction = data.get(entity, ActiveAction.class);
            if (data.has(activeAction.action(), ActionEvent.class)) {
                // don't cancel event actions
                continue;
            }

            Integer best = findNearestEnemy(data, entity);
            if (best != null) {
                Position position = data.get(entity, Position.class);
                Position otherPosition = data.get(best, Position.class);
                Vector2 delta = otherPosition.vector().sub(position.vector());
                Direction direction = Direction.of(delta.x(), delta.y());

                int meleeAction = getAction(data, entity, ActionKey.ATTACK1);
                int rangeAction = getAction(data, entity, ActionKey.ATTACK2);
                Integer dashAction = getAction(data, entity, ActionKey.DASH);

                int meleeRange = 1000;
                int rangedRange = 5000;
                int dashRange = 4000;
                if (!data.has(meleeAction, ActiveCooldown.class) && delta.squaredLength() < meleeRange * meleeRange) {
                    data.set(entity, new ActorInput(direction, ActionKey.ATTACK1));
                } else if (!data.has(rangeAction, ActiveCooldown.class) && delta.squaredLength() < rangedRange * rangedRange) {
                    data.set(entity, new ActorInput(direction, ActionKey.ATTACK2));
                } else if (dashAction != null && !data.has(dashAction, ActiveCooldown.class) && delta.squaredLength() > dashRange * dashRange) {
                    data.set(entity, new ActorInput(direction, ActionKey.DASH));
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

    private Integer getAction(EntityData data, int actor, ActionKey key) {
        for (int action : data.findByValue(new ActionOf(actor))) {
            if (data.get(action, ActionKey.class) == key) {
                return action;
            }
        }
        return null;
    }
}
