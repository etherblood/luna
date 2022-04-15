package com.etherblood.luna.network.server;

import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.network.api.PlaybackBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGame {

    private final GameEngine state;
    private final Map<Integer, ServerEventMessageBuilder> builders = new ConcurrentHashMap<>();
    private final PlaybackBuffer buffer = new PlaybackBuffer();

    public ServerGame(GameEngine state) {
        this.state = state;
    }

    public GameEngine getState() {
        return state;
    }

    public Map<Integer, ServerEventMessageBuilder> getBuilders() {
        return builders;
    }

    public PlaybackBuffer getBuffer() {
        return buffer;
    }
}
