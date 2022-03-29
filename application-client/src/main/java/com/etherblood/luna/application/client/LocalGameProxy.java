package com.etherblood.luna.application.client;

import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalGameProxy implements GameProxy {

    private static final long MILLIS_PER_SECOND = 1000;
    private final GameEngine engine;
    private final int playerId;
    private final Set<PlayerInput> pendingInputs = new HashSet<>();
    private final long fps;

    public LocalGameProxy(GameEngine engine, int playerId, long fps) {
        this.engine = engine;
        this.playerId = playerId;
        this.fps = fps;
    }

    @Override
    public GameEngine getEngine() {
        return engine;
    }

    @Override
    public void requestInput(PlayerInput input) {
        pendingInputs.add(input);
    }

    @Override
    public int getPlayer() {
        return playerId;
    }

    @Override
    public void update() {
        while (engine.getStartEpochMillis() + MILLIS_PER_SECOND * engine.getFrame() / fps <= System.currentTimeMillis()) {
            engine.tick(pendingInputs.stream().map(x -> new GameEvent(x)).collect(Collectors.toSet()));
            pendingInputs.clear();
        }
    }
}
