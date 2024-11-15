package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActiveAction;
import com.etherblood.luna.engine.ActorInput;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.PendingDelete;
import com.etherblood.luna.engine.actions.data.ActionDuration;
import com.etherblood.luna.engine.actions.data.ActionInterruptResist;
import com.etherblood.luna.engine.actions.data.ActionInterruptStrength;
import com.etherblood.luna.engine.actions.data.ActionOf;
import com.etherblood.luna.engine.actions.data.ActionSpeed;
import com.etherblood.luna.engine.actions.data.ActionTurnable;
import com.etherblood.luna.engine.actions.data.ActionType;
import com.etherblood.luna.engine.actions.data.ActiveCooldown;
import com.etherblood.luna.engine.actions.data.BaseCooldown;
import com.etherblood.luna.engine.actions.data.DeleteAfterActorAction;
import com.etherblood.luna.engine.damage.MilliHealth;
import com.etherblood.luna.engine.movement.Speed;

import java.util.Objects;

public class ActionActivateSystem implements GameSystem {

    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int entity : data.list(ActiveAction.class)) {
            ActiveAction state = data.get(entity, ActiveAction.class);
            int idleAction = Objects.requireNonNull(getAction(data, entity, ActionType.IDLE),
                    "No valid idle action available.");
            int activeAction = state.action();

            // handle ended actions
            ActionDuration duration = data.get(activeAction, ActionDuration.class);
            if (duration != null && game.getFrame() - state.startFrame() > duration.frames()) {
                activeAction = switchAction(game, entity, activeAction, idleAction);
            }

            // handle user input
            ActorInput input = data.get(entity, ActorInput.class);

            // fall
            MilliHealth health = data.get(entity, MilliHealth.class);
            Integer fallenAction = getAction(data, entity, ActionType.FALLEN);
            if (health != null && health.value() <= 0 && fallenAction != null) {
                Direction direction = input == null ? null : input.direction();
                // TODO: this is hacky, improve?
                // overwrite user input with FALLEN action
                input = new ActorInput(direction, ActionType.FALLEN);
            }

            Integer inputAction = input != null ? getAction(data, entity, input.action()) : null;
            if (inputAction != null) {
                // fall back to walk/idle if inputAction is on cooldown
                if (data.has(inputAction, ActiveCooldown.class)) {
                    Integer walkAction = getAction(data, entity, ActionType.WALK);
                    if (input.direction() != null && walkAction != null) {
                        inputAction = walkAction;
                    } else {
                        inputAction = idleAction;
                    }
                }
                if (!data.has(inputAction, ActiveCooldown.class) && isInterrupt(game, entity, activeAction, inputAction)) {
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
        return newAction;
    }

    private void actorSetupBeforeAction(GameEngine game, int entity, int action) {
        EntityData data = game.getData();
        ActionSpeed speed = data.get(action, ActionSpeed.class);
        if (speed != null) {
            data.set(entity, new Speed(speed.milliMetresPerFrame()));
        }
        BaseCooldown cooldown = data.get(action, BaseCooldown.class);
        if (cooldown != null) {
            data.set(action, new ActiveCooldown(game.getFrame() + cooldown.frames()));
        }
    }

    private void actorCleanupAfterAction(GameEngine game, int entity, int action) {
        EntityData data = game.getData();
        data.remove(entity, Speed.class);
        for (int other : data.findByValue(new OwnedBy(entity))) {
            if (data.has(other, DeleteAfterActorAction.class)) {
                data.set(other, new PendingDelete(game.getFrame()));
            }
        }
    }

    private boolean isInterrupt(GameEngine game, int actor, int activeAction, int inputAction) {
        if (activeAction == inputAction) {
            return false;
        }
        EntityData data = game.getData();
        ActionInterruptStrength strength = data.get(inputAction, ActionInterruptStrength.class);
        int strengthLevel = strength == null ? ActionInterruptResist.NONE : strength.level();
        ActionInterruptResist resist = data.get(activeAction, ActionInterruptResist.class);
        int resistLevel = ActionInterruptResist.NONE;
        if (resist != null && resist.frames() > game.getFrame() - data.get(actor, ActiveAction.class).startFrame()) {
            resistLevel = resist.level();
        }
        return strengthLevel >= resistLevel;
    }

    private Integer getAction(EntityData data, int actor, ActionType key) {
        for (int action : data.findByValue(new ActionOf(actor))) {
            if (data.get(action, ActionType.class) == key) {
                return action;
            }
        }
        return null;
    }
}
