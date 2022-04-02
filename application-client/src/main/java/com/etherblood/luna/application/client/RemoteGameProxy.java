package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.network.client.ClientGameModule;
import com.etherblood.luna.network.client.timestamp.ClientTimestampModule;

public class RemoteGameProxy implements GameProxy {

    private final ClientTimestampModule timestampModule;
    private final ClientGameModule gameModule;
    private final JwtAuthenticationUser player;

    public RemoteGameProxy(ClientTimestampModule timestampModule, ClientGameModule gameModule, JwtAuthenticationUser player) {
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

        if (timestampModule.isInitialized()) {
            long approxServerTime = timestampModule.getApproxServerTime();
            gameModule.run(approxServerTime, 60, new GameEvent(input, null));
        }
    }

    @Override
    public long getLatency() {
        return timestampModule.getLatency();
    }
}
