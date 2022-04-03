package com.etherblood.luna.engine.actions;

import java.util.Map;

public class ActionFactory {

    private final Map<ActionKey, Action> actionMappings = Map.of(
            ActionKey.IDLE, new Idle(),
            ActionKey.WALK, new Walk(),
            ActionKey.DASH, new Dash(),
            ActionKey.ATTACK1, new GazeOfDarkness(),
            ActionKey.ATTACK2, new BladeOfChaos(),
            ActionKey.FALLEN, new Fallen()
    );

    public Action getAction(ActionKey key) {
        Action action = actionMappings.get(key);
        if (action == null) {
            throw new NullPointerException("No mapping for " + key);
        }
        return action;
    }
}
