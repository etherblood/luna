package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.Action;
import com.etherblood.luna.engine.actions.ActionFactory;

public class ApplyActionSystem implements GameSystem {
    private final ActionFactory factory;

    public ApplyActionSystem(ActionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(ActorState.class)) {
            ActorState state = data.get(entity, ActorState.class);
            Action action = factory.getAction(state.action(), engine.getFrame() - state.startFrame());
            action.update(engine, entity);
        }
    }
}
