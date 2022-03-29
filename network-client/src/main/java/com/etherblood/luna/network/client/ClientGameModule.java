package com.etherblood.luna.network.client;

import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import com.etherblood.luna.network.api.GameModule;
import com.etherblood.luna.network.api.PlaybackBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class ClientGameModule extends GameModule {

    public static final int MILLIS_PER_SECOND = 1000;
    private AtomicReference<GameEngine> stateReference = new AtomicReference<>(null);
    private final ClientEventMessageBuilder builder = new ClientEventMessageBuilder();
    private final PlaybackBuffer buffer = new PlaybackBuffer();
    private final int delayFrames = 2;
    private final Connection connection;

    public ClientGameModule(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof GameEngine state) {
            System.out.println("received: " + state);
            stateReference.set(state);
        } else if (object instanceof EventMessage message) {
            builder.updateAck(message);
            for (EventMessagePart part : message.parts()) {
                buffer.buffer(part.frame(), part.event());
            }
            // TODO: apply latest locked frame/clear frame
        }
    }

    public void input(GameEvent event) {
        GameEngine state = stateReference.get();
        if (state != null) {
            builder.enqueueAction(new EventMessagePart(state.getFrame() + delayFrames, event));
        }
    }

    public void run(long servertime, int fps) {
        // TODO: fps should be taken from game settings?
        GameEngine state = stateReference.get();
        if (state != null) {
            boolean stepped = false;
            while (state.getStartEpochMillis() + MILLIS_PER_SECOND * state.getFrame() / fps <= servertime) {
                long frame = state.getFrame();
                state.tick(buffer.peek(frame));
                buffer.clear(frame);
                stepped = true;
            }
            if (stepped) {
                connection.sendUDP(builder.build());
            }
        }
    }

    public GameEngine getState() {
        return stateReference.get();
    }
}
