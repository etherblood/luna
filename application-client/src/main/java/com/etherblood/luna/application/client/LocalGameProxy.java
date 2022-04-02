package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import java.util.Set;

public class LocalGameProxy implements GameProxy {

    private static final long MILLIS_PER_SECOND = 1000;
    private final GameEngine engine;
    private final JwtAuthenticationUser player;
    private final long fps;

    public LocalGameProxy(GameEngine engine, JwtAuthenticationUser player, long fps) {
        this.engine = engine;
        this.player = player;
        this.fps = fps;
    }

    @Override
    public GameEngine getEngineSnapshot() {
        return engine;
    }

    @Override
    public JwtAuthenticationUser getPlayer() {
        return player;
    }

    @Override
    public void update(PlayerInput input) {
        while (engine.getStartEpochMillis() + MILLIS_PER_SECOND * engine.getFrame() / fps <= System.currentTimeMillis()) {
            engine.tick(Set.of(new GameEvent(input, null)));
        }
    }

    @Override
    public long getLatency() {
        return 0;
    }
}
