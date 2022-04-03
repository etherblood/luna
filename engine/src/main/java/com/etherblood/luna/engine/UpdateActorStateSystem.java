package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.Action;
import com.etherblood.luna.engine.actions.ActionFactory;
import com.etherblood.luna.engine.actions.ActionKey;

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
            Action action = factory.getAction(state.action(), engine.getFrame() - state.startFrame());

            // handle ended actions
            if (action.hasEnded()) {
                state = new ActorState(ActionKey.IDLE, engine.getFrame());
                action = factory.getAction(state.action(), engine.getFrame() - state.startFrame());
            }

            // handle user input
            PlayerInput input = data.get(entity, PlayerInput.class);
            if (input != null) {
                if (action.isInterruptedBy(input.action())) {
                    state = new ActorState(input.action(), engine.getFrame());
                    if (input.direction() != null) {
                        data.set(entity, input.direction());
                    }
                } else if (action.isTurnable()) {
                    if (input.direction() != null) {
                        data.set(entity, input.direction());
                    }
                }
                data.remove(entity, PlayerInput.class);
            }

            // set new state
            data.set(entity, state);
        }
    }
}
