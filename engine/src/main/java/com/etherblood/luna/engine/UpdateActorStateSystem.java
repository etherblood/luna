package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.Action;
import com.etherblood.luna.engine.actions.ActionFactory;
import com.etherblood.luna.engine.actions.ActionKey;
import java.util.Map;

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
                // TODO: this is hacky, improve
                input = new ActorInput(input.direction(), ActionKey.FALLEN);
            }

            if (input != null && skillMap.containsKey(input.action())) {
                if (action.isInterruptedBy(engine, entity, input.action())) {
                    action.cleanup(engine, entity);
                    state = new ActorState(skillMap.get(input.action()), engine.getFrame());
                    if (input.direction() != null) {
                        data.set(entity, input.direction());
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
