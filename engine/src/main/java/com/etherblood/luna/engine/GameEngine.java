package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import java.util.Collection;
import java.util.List;

public class GameEngine {

    private final GameRules rules;
    private final long startEpochMillis;
    private long tick;
    private final EntityData data;
    private final List<GameSystem> systems;

    public GameEngine(GameRules rules, long startEpochMillis, long tick) {
        this(rules, startEpochMillis, new EntityDataImpl(rules.getComponentTypes()), tick);
    }

    public GameEngine(GameRules rules, long startEpochMillis, EntityData data, long tick) {
        this.rules = rules;
        this.startEpochMillis = startEpochMillis;
        this.data = data;
        this.systems = rules.getSystems();
        this.tick = tick;
    }

    public void tick(Collection<GameEvent> events) {
        for (GameEvent event : events) {
            if (event.input() != null) {
                int player = event.input().player();
                data.set(player, event.input());
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

    public long getFrame() {
        return tick;
    }

    public EntityData getData() {
        return data;
    }

    public long getStartEpochMillis() {
        return startEpochMillis;
    }

}
