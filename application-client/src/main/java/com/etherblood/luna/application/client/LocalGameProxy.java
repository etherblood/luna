package com.etherblood.luna.application.client;

import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalGameProxy implements GameProxy {

    private final GameEngine engine;
    private final int playerId;
    private final Set<PlayerInput> pendingInputs = new HashSet<>();

    public LocalGameProxy(GameEngine engine, int playerId) {
        this.engine = engine;
        this.playerId = playerId;
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
        // TODO: check if it is time to tick
        engine.tick(pendingInputs.stream().map(x -> new GameEvent(x)).collect(Collectors.toSet()));
        pendingInputs.clear();
    }
}
