package com.etherblood.luna.network.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import com.etherblood.luna.network.api.GameModule;
import com.etherblood.luna.network.api.PlaybackBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    public synchronized void received(Connection connection, Object object) {
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

    public synchronized void input(GameEvent event) {
        GameEngine state = stateReference.get();
        if (state != null) {
            builder.enqueueAction(new EventMessagePart(state.getFrame() + delayFrames, event));
        }
    }

    public synchronized void run(long servertime, int fps) {
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

    public synchronized GameEngine getStateSnapshot() {
        GameEngine engine = stateReference.get();
        if (engine == null) {
            return null;
        }

        // dirty hack to copy state
        Kryo kryo = connection.getEndPoint().getKryo();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output out = new Output(stream);
        kryo.writeObject(out, engine);
        out.flush();

        Input in = new Input(new ByteArrayInputStream(stream.toByteArray()));
        GameEngine copy = kryo.readObject(in, GameEngine.class);
        return copy;
    }
}
