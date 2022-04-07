package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.actions.amara.BladeOfChaos;
import com.etherblood.luna.engine.actions.amara.GazeOfDarkness;
import com.etherblood.luna.engine.actions.amara.GhostSpell;
import com.etherblood.luna.engine.actions.ghost.MeleeAttack;
import java.util.HashMap;
import java.util.Map;

public class ActionFactory {

    // TODO: this class should be located close to templates

    private final Map<String, Action> actionMappings;

    {
        Map<String, Action> amaraMap = Map.of(
                "amara.idle", new Idle(),
                "amara.walk", new Walk(1100),
                "amara.dash", new Dash(48, 5000),
                "amara.gaze_of_darkness", new GazeOfDarkness(),
                "amara.blade_of_chaos", new BladeOfChaos(),
                "amara.fallen", new Fallen(179)
        );
        Map<String, Action> ghostMap = Map.of(
                "ghost.idle", new Idle(),
                "ghost.fly_forward", new Walk(3000),
                "ghost.melee_attack", new MeleeAttack(48, 72, 72,
                        750, 500, 3_000),
                "ghost.ghost_spell", new GhostSpell(),
                "ghost.die", new Fallen(100)
        );
        actionMappings = new HashMap<>();
        actionMappings.putAll(amaraMap);
        actionMappings.putAll(ghostMap);
    }

    public Action getAction(String id) {
        Action action = actionMappings.get(id);
        if (action == null) {
            throw new NullPointerException("No mapping for " + id);
        }
        return action;
    }
}
