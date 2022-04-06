package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.actions.amara.BladeOfChaos;
import com.etherblood.luna.engine.actions.amara.GazeOfDarkness;
import com.etherblood.luna.engine.actions.ghost.MeleeAttack;
import java.util.Map;

public class ActionFactory {

    // TODO: this class should be located close to templates

    private final Map<String, Action> actionMappings = Map.of(
            "amara.idle", new Idle(),
            "amara.walk", new Walk(1000),
            "amara.dash", new Dash(48, 3000),
            "amara.gaze_of_darkness", new GazeOfDarkness(),
            "amara.blade_of_chaos", new BladeOfChaos(),

            "ghost.idle", new Idle(),
            "ghost.fly_forward", new Walk(2500),
            "ghost.melee_attack", new MeleeAttack(48, 72, 72,
                    500, 750, 30_000)
    );

    public Action getAction(String id) {
        Action action = actionMappings.get(id);
        if (action == null) {
            throw new NullPointerException("No mapping for " + id);
        }
        return action;
    }
}
