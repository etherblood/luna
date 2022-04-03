package com.etherblood.luna.network.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import com.etherblood.luna.network.api.GameModule;
import com.etherblood.luna.network.api.PlaybackBuffer;

public class ClientGameModule extends GameModule {

    public static final int MILLIS_PER_SECOND = 1000;
    private GameEngine state = null;
    private final ClientEventMessageBuilder builder = new ClientEventMessageBuilder();
    private final PlaybackBuffer buffer = new PlaybackBuffer();
    private final Connection connection;
    private final int inputDelayFrames = 2;
    private long serverFrame = -1;

    public ClientGameModule(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized void received(Connection connection, Object object) {
        if (object instanceof GameEngine state) {
            this.state = state;
            this.serverFrame = state.getFrame();
        } else if (object instanceof EventMessage message) {
            builder.updateAck(message);
            for (EventMessagePart part : message.parts()) {
                if (!buffer.buffer(part.frame(), part.event())) {
                    // drop part, it is a duplicate of an already handled one
                }
            }
            for (long frame = state.getFrame(); frame <= message.lockFrame(); frame++) {
                state.tick(buffer.peek(frame));
                buffer.clear(frame);
            }
        }
    }

    public synchronized void run(long servertime, GameEvent input) {
        long nextFrame = (servertime - state.getStartEpochMillis()) * state.getRules().getFramesPerSecond() / MILLIS_PER_SECOND;
        if (serverFrame < nextFrame) {
            do {
                serverFrame++;
                builder.enqueueAction(new EventMessagePart(serverFrame + inputDelayFrames, input));
            } while (serverFrame < nextFrame);
            connection.sendUDP(builder.build());
        }
    }

    public synchronized GameEngine getStateSnapshot() {
        if (state == null) {
            return null;
        }
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setCopyReferences(false);
        initialize(kryo);
        GameEngine copy = kryo.copy(state);
        for (long frame = state.getFrame(); frame < serverFrame; frame++) {
            copy.tick(buffer.peek(frame));
        }
        return copy;
    }
}
