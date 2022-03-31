package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class UpdateActorStateSystem implements GameSystem {

    private static final long DASH_DURATION_TICKS = 48;

    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(ActorState.class)) {
            ActorState state = data.get(entity, ActorState.class);
            if (state != null) {
                if (state.action() == ActorAction.DASH && state.startFrame() + DASH_DURATION_TICKS <= engine.getFrame()) {
                    state = new ActorState(ActorAction.IDLE, state.direction(), engine.getFrame());
                }
            } else {
                continue;
            }

            PlayerInput input = data.get(entity, PlayerInput.class);
            if (input != null) {
                if (input.action().interrupts(state.action())) {
                    state = new ActorState(input.action(), input.direction(), engine.getFrame());
                } else if (state.action().isTurnable() && state.direction() != input.direction()) {
                    state = new ActorState(state.action(), input.direction(), state.startFrame());
                }
            }
            data.set(entity, state);
        }
    }
}
