package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class ApplyActionSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(ActorState.class)) {
            ActorState state = data.get(entity, ActorState.class);
            ActorAction action = state.action();
            if (action == ActorAction.IDLE) {
                data.set(entity, new Speed(state.direction().toLengthVector(1000 / 60)));
            }
        }
    }
}
