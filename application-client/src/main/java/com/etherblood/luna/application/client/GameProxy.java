package com.etherblood.luna.application.client;

import com.etherblood.luna.engine.GameEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameProxy {

    private final GameEngine engine;
    private final int playerId;
    private final Map<Integer, Set<Object>> pendingInputs = new HashMap<>();

    public GameProxy(GameEngine engine, int playerId) {
        this.engine = engine;
        this.playerId = playerId;
    }

    public GameEngine getEngine() {
        return engine;
    }

    public void requestInput(Set<Object> input) {
        pendingInputs.put(playerId, input);
    }

    public void tick() {
        engine.tick(pendingInputs);
        pendingInputs.clear();
    }

    public int getPlayer() {
        return playerId;
    }
}
