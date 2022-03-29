package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class UpdateActionSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(ActorState.class)) {
            ActorState state = data.get(entity, ActorState.class);
            int player = data.get(entity, OwnedBy.class).player();
            PlayerInput input = data.get(player, PlayerInput.class);
            if (input != null && ((input.action() != ActorAction.IDLE && state.action() != input.action() && state.action().isInterruptible())
                    || (state.action() == input.action() && state.action().isTurnable() && state.direction() != input.direction()))) {
                data.set(entity, new ActorState(input.action(), input.direction(), engine.getFrame()));
            }
        }
    }
}
