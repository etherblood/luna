package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActiveAction;
import com.etherblood.luna.engine.ActorInput;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.actions.data.ActionCooldown;
import com.etherblood.luna.engine.actions.data.ActionDuration;
import com.etherblood.luna.engine.actions.data.ActionInterruptResist;
import com.etherblood.luna.engine.actions.data.ActionInterruptStrength;
import com.etherblood.luna.engine.actions.data.ActionKey;
import com.etherblood.luna.engine.actions.data.ActionOf;
import com.etherblood.luna.engine.actions.data.ActionSpeed;
import com.etherblood.luna.engine.actions.data.ActionTurnable;
import com.etherblood.luna.engine.actions.data.ActiveCooldown;
import com.etherblood.luna.engine.damage.MilliHealth;
import com.etherblood.luna.engine.movement.Speed;

public class ActionActivateSystem implements GameSystem {

    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int entity : data.list(ActiveAction.class)) {
            ActiveAction state = data.get(entity, ActiveAction.class);
            int activeAction = state.action();

            // handle ended actions
            ActionDuration duration = data.get(activeAction, ActionDuration.class);
            if (duration != null && game.getFrame() - state.startFrame() > duration.frames()) {
                int idleAction = getAction(data, entity, ActionKey.IDLE);// assume IDLE always exists
                activeAction = switchAction(game, entity, activeAction, idleAction);
            }

            // handle user input
            ActorInput input = data.get(entity, ActorInput.class);

            // fall
            MilliHealth health = data.get(entity, MilliHealth.class);
            Integer fallenAction = getAction(data, entity, ActionKey.FALLEN);
            if (health != null && health.value() <= 0 && fallenAction != null) {
                Direction direction = input == null ? null : input.direction();
                // TODO: this is hacky, improve
                // overwrite user input with FALLEN action
                input = new ActorInput(direction, ActionKey.FALLEN);
            }

            Integer inputAction = input != null ? getAction(data, entity, input.action()) : null;
            if (inputAction != null) {
                // TODO: if an action on cooldown is used while a direction is being held, it should be handled as if the input was WALK
                if (!data.has(inputAction, ActiveCooldown.class) && isInterrupt(data, activeAction, inputAction)) {
                    activeAction = switchAction(game, entity, activeAction, inputAction);
                    if (input.direction() != null) {
                        data.set(entity, input.direction());
                    }
                } else if (data.has(activeAction, ActionTurnable.class)) {
                    if (input.direction() != null) {
                        data.set(entity, input.direction());
                    }
                }
                data.remove(entity, ActorInput.class);
            }
        }
    }

    private int switchAction(GameEngine game, int entity, int activeAction, int newAction) {
        EntityData data = game.getData();
        actorCleanupAfterAction(game, entity, activeAction);
        data.set(entity, new ActiveAction(newAction, game.getFrame()));
        actorSetupBeforeAction(game, entity, newAction);
        return activeAction;
    }

    private void actorSetupBeforeAction(GameEngine game, int entity, int action) {
        EntityData data = game.getData();
        ActionSpeed speed = data.get(action, ActionSpeed.class);
        if (speed != null) {
            data.set(entity, new Speed(speed.milliMetresPerFrame()));
        }
        ActionCooldown cooldown = data.get(action, ActionCooldown.class);
        if (cooldown != null) {
            data.set(action, new ActiveCooldown(game.getFrame() + cooldown.frames()));
        }
    }

    private void actorCleanupAfterAction(GameEngine game, int entity, int action) {
        EntityData data = game.getData();
        data.remove(entity, Speed.class);
    }

    private boolean isInterrupt(EntityData data, int activeAction, int inputAction) {
        if (activeAction == inputAction) {
            return false;
        }
        ActionInterruptStrength strength = data.get(inputAction, ActionInterruptStrength.class);
        int strengthLevel = strength == null ? ActionInterruptResist.NONE : strength.level();
        ActionInterruptResist resist = data.get(activeAction, ActionInterruptResist.class);
        int resistLevel = resist == null ? ActionInterruptResist.NONE : resist.level();
        return strengthLevel >= resistLevel;
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
