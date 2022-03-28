package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void tick(Map<Integer, Set<Object>> playerActions) {
        for (Map.Entry<Integer, Set<Object>> entry : playerActions.entrySet()) {
            int player = entry.getKey();
            for (Object object : entry.getValue()) {
                if (object instanceof PlayerInput input) {
                    data.set(player, input);
                }
            }
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
