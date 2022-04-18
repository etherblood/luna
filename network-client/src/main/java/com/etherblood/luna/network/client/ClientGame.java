package com.etherblood.luna.network.client;

import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.network.api.game.PlaybackBuffer;

public class ClientGame {
    private GameEngine state;
    private ClientEventMessageBuilder builder;
    private PlaybackBuffer buffer;
    private long serverFrame;

    public ClientGame(GameEngine state) {
        this.state = state;
        builder = new ClientEventMessageBuilder(state.getId());
        buffer = new PlaybackBuffer();
        serverFrame = state.getFrame();
    }

    public GameEngine getState() {
        return state;
    }

    public ClientEventMessageBuilder getBuilder() {
        return builder;
    }

    public PlaybackBuffer getBuffer() {
        return buffer;
    }

    public long getServerFrame() {
        return serverFrame;
    }

    public void incServerFrame() {
        serverFrame++;
    }
}
