package com.etherblood.luna.engine.actions;

import java.util.Map;
import java.util.function.LongFunction;

public class ActionFactory {

    private final Map<ActionKey, LongFunction<Action>> actionMappings = Map.of(
            ActionKey.IDLE, Idle::new,
            ActionKey.WALK, Walk::new,
            ActionKey.DASH, Dash::new,
            ActionKey.ATTACK1, Attack1::new,
            ActionKey.ATTACK2, Attack2::new
    );

    public Action getAction(ActionKey key, long elapsedFrames) {
        LongFunction<Action> constructor = actionMappings.get(key);
        if (constructor == null) {
            throw new NullPointerException("No mapping for " + key);
        }
        return constructor.apply(elapsedFrames);
    }
}
