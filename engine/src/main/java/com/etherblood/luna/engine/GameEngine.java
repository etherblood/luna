package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import java.util.List;
import java.util.Map;

public class GameEngine {

    private final GameRules rules;
    private long tick;
    private final EntityData data;
    private final List<GameSystem> systems;

    public GameEngine(GameRules rules) {
        this(rules, new EntityDataImpl(rules.getComponentTypes()), rules.getSystems());
    }

    public GameEngine(GameRules rules, EntityData data, List<GameSystem> systems) {
        this.rules = rules;
        this.data = data;
        this.systems = systems;
        tick = 0;
    }

    public void tick(Map<Integer, PlayerInput> playerActions) {
        for (Map.Entry<Integer, PlayerInput> entry : playerActions.entrySet()) {
            int player = entry.getKey();
            PlayerInput actions = entry.getValue();
            data.set(player, actions);
        }
        tick++;
        for (GameSystem system : systems) {
            system.tick(this);
        }
    }

    public GameRules getRules() {
        return rules;
    }

    public long getTick() {
        return tick;
    }

    public EntityData getData() {
        return data;
    }
}
