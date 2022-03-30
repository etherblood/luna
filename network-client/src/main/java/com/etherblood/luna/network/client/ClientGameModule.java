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
    private long serverFrame = -1;
    private final ClientEventMessageBuilder builder = new ClientEventMessageBuilder();
    private final PlaybackBuffer buffer = new PlaybackBuffer();
    private final int delayFrames = 2;
    private final Connection connection;

    public ClientGameModule(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized void received(Connection connection, Object object) {
        if (object instanceof GameEngine state) {
            System.out.println("received: " + state);
            this.state = state;
        } else if (object instanceof EventMessage message) {
            builder.updateAck(message);
            for (EventMessagePart part : message.parts()) {
                if (!buffer.buffer(part.frame(), part.event())) {
                    //TODO
                }
            }
            for (long frame = state.getFrame(); frame <= message.lockFrame(); frame++) {
                state.tick(buffer.peek(frame));
                buffer.clear(frame);
            }
        }
    }

    public synchronized void input(GameEvent event) {
        builder.enqueueAction(new EventMessagePart(serverFrame + delayFrames, event));
    }

    public synchronized void run(long servertime, int fps) {
        long nextFrame = (servertime - state.getStartEpochMillis()) * fps / MILLIS_PER_SECOND;
        if (nextFrame != serverFrame) {
            serverFrame = nextFrame;
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
