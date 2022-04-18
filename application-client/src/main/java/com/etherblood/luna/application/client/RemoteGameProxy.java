package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.network.client.GameClientModule;
import com.etherblood.luna.network.client.timestamp.TimestampClientModule;

public class RemoteGameProxy implements GameProxy {

    private final TimestampClientModule timestampModule;
    private final GameClientModule gameModule;
    private final JwtAuthenticationUser player;

    public RemoteGameProxy(TimestampClientModule timestampModule, GameClientModule gameModule, JwtAuthenticationUser player) {
        this.timestampModule = timestampModule;
        this.gameModule = gameModule;
        this.player = player;
    }

    @Override
    public GameEngine getEngineSnapshot() {
        return gameModule.getStateSnapshot();
    }

    @Override
    public JwtAuthenticationUser getPlayer() {
        return player;
    }

    @Override
    public void update(PlayerInput input) {
        timestampModule.run();
        long approxServerTime = timestampModule.getApproxServerTime();
        gameModule.run(approxServerTime, new GameEvent(input, null));
    }

    @Override
    public long getLatency() {
        return timestampModule.getLatency();
    }
}
