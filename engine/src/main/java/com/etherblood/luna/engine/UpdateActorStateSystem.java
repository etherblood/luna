package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.Action;
import com.etherblood.luna.engine.actions.ActionFactory;
import com.etherblood.luna.engine.actions.ActionKey;
import com.etherblood.luna.engine.actions.Attack1Cooldown;
import com.etherblood.luna.engine.actions.Attack2Cooldown;
import com.etherblood.luna.engine.actions.CooldownComponent;
import com.etherblood.luna.engine.damage.MilliHealth;
import java.util.Map;
import java.util.function.LongFunction;

public class UpdateActorStateSystem implements GameSystem {

    private final ActionFactory factory;

    public UpdateActorStateSystem(ActionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(ActorState.class)) {
            ActorState state = data.get(entity, ActorState.class);
            Action action = factory.getAction(state.actionId());

            Map<ActionKey, String> skillMap = data.get(entity, SkillSet.class).skillMap();
            // handle ended actions
            if (action.hasEnded(engine, entity)) {
                action.cleanup(engine, entity);
                state = new ActorState(skillMap.get(ActionKey.IDLE), engine.getFrame());
                action = factory.getAction(state.actionId());
            }

            // handle user input
            ActorInput input = data.get(entity, ActorInput.class);

            // fall
            MilliHealth health = data.get(entity, MilliHealth.class);
            if (health != null && health.value() <= 0 && skillMap.containsKey(ActionKey.FALLEN)) {
                Direction direction = input == null ? null : input.direction();
                // TODO: this is hacky, improve
                input = new ActorInput(direction, ActionKey.FALLEN);
            }

            if (input != null && skillMap.containsKey(input.action())) {
                Map<ActionKey, Class<? extends CooldownComponent>> cooldownMappings = Map.of(
                        ActionKey.ATTACK1, Attack1Cooldown.class,
                        ActionKey.ATTACK2, Attack2Cooldown.class
                );
                Class<? extends CooldownComponent> cooldownType = cooldownMappings.get(input.action());
                boolean onCooldown = cooldownType != null && data.has(entity, cooldownType);

                if (!onCooldown && action.isInterruptedBy(engine, entity, input.action())) {
                    action.cleanup(engine, entity);
                    state = new ActorState(skillMap.get(input.action()), engine.getFrame());
                    action = factory.getAction(state.actionId());
                    if (input.direction() != null) {
                        data.set(entity, input.direction());
                    }
                    Long cooldownFrames = action.cooldownFrames(engine, entity);
                    if (cooldownFrames != null) {
                        Map<ActionKey, LongFunction<? extends CooldownComponent>> cooldownConstructors = Map.of(
                                ActionKey.ATTACK1, Attack1Cooldown::new,
                                ActionKey.ATTACK2, Attack2Cooldown::new);
                        LongFunction<? extends CooldownComponent> cooldownConstructor = cooldownConstructors.get(input.action());
                        data.set(entity, cooldownConstructor.apply(engine.getFrame() + cooldownFrames));
                    }
                } else if (action.isTurnable(engine, entity)) {
                    if (input.direction() != null) {
                        data.set(entity, input.direction());
                    }
                }
                data.remove(entity, ActorInput.class);
            }

            // set new state
            data.set(entity, state);
        }
    }
}
