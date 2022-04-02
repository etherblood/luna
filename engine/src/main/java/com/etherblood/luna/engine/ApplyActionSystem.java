package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class ApplyActionSystem implements GameSystem {

    public static final int WALK_SPEED = 1000 / 60;
    public static final int DASH_SPEED = 3000 / 60;

    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(ActorState.class)) {
            ActorState state = data.get(entity, ActorState.class);
            Direction direction = data.get(entity, Direction.class);
            ActorAction action = state.action();
            if (action == ActorAction.WALK) {
                data.set(entity, new Speed(direction.toLengthVector(WALK_SPEED)));
            } else if (action == ActorAction.DASH) {
                data.set(entity, new Speed(direction.toLengthVector(DASH_SPEED)));
            } else {
                data.set(entity, new Speed(0, 0));
            }
        }
    }
}
