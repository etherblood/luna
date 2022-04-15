package com.etherblood.luna.network.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import com.etherblood.luna.network.api.GameModule;
import com.etherblood.luna.network.api.PlaybackBuffer;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class ClientGameModule extends GameModule {

    public static final int MILLIS_PER_SECOND = 1000;
    private final Connection connection;
    private final int inputDelayFrames = 2;
    private ClientGame clientGame;

    public ClientGameModule(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized void received(Connection connection, Object object) {
        if (object instanceof GameEngine game) {
            clientGame = new ClientGame(game);
        } else if (object instanceof EventMessage message) {
            if (clientGame != null && clientGame.getState().getId().equals(message.gameId())) {
                ClientEventMessageBuilder builder = clientGame.getBuilder();
                PlaybackBuffer buffer = clientGame.getBuffer();
                GameEngine state = clientGame.getState();
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
    }

    public synchronized void run(long servertime, GameEvent input) {
        if (clientGame != null) {
            ClientEventMessageBuilder builder = clientGame.getBuilder();
            GameEngine state = clientGame.getState();
            long nextFrame = (servertime - state.getStartEpochMillis()) * state.getRules().getFramesPerSecond() / MILLIS_PER_SECOND;
            if (clientGame.getServerFrame() < nextFrame) {
                do {
                    clientGame.incServerFrame();
                    builder.enqueueAction(new EventMessagePart(clientGame.getServerFrame() + inputDelayFrames, input));
                } while (clientGame.getServerFrame() < nextFrame);
                connection.sendUDP(builder.build());
            }
        }
    }

    public synchronized GameEngine getStateSnapshot() {
        if (clientGame == null) {
            return null;
        }
        PlaybackBuffer buffer = clientGame.getBuffer();
        GameEngine state = clientGame.getState();
        long serverFrame = clientGame.getServerFrame();
        Kryo kryo = getKryo();
        GameEngine copy = kryo.copy(state);
        for (long frame = state.getFrame(); frame < serverFrame; frame++) {
            copy.tick(buffer.peek(frame));
        }
        return copy;
    }

    private Kryo getKryo() {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setCopyReferences(false);
        initialize(kryo);
        return kryo;
    }

    private void logStateHash(GameEngine state) {
        Kryo kryo = getKryo();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        kryo.writeObject(output, state);
        output.flush();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(outputStream.toByteArray());
            System.out.println("State hash on frame " + state.getFrame() + ": " + byteArray2Hex(hashBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
