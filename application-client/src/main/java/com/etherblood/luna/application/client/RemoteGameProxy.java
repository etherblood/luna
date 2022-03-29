package com.etherblood.luna.application.client;

import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.network.client.ClientGameModule;
import com.etherblood.luna.network.client.timestamp.ClientTimestampModule;

public class RemoteGameProxy implements GameProxy {

    private final ClientTimestampModule timestampModule;
    private final ClientGameModule gameModule;
    private final int player;

    public RemoteGameProxy(ClientTimestampModule timestampModule, ClientGameModule gameModule, int player) {
        this.timestampModule = timestampModule;
        this.gameModule = gameModule;
        this.player = player;
    }

    @Override
    public GameEngine getEngineSnapshot() {
        return gameModule.getStateSnapshot();
    }

    @Override
    public void requestInput(PlayerInput input) {
        gameModule.input(new GameEvent(input));
    }

    @Override
    public int getPlayer() {
        return player;
    }

    @Override
    public void update() {
        timestampModule.run();

        long approxServerTime = timestampModule.getApproxServerTime();
        gameModule.run(approxServerTime, 60);
    }
}
