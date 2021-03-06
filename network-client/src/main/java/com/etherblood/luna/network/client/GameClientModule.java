package com.etherblood.luna.network.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.network.api.game.GameModule;
import com.etherblood.luna.network.api.game.PlaybackBuffer;
import com.etherblood.luna.network.api.game.messages.EnterGameRequest;
import com.etherblood.luna.network.api.game.messages.EventMessage;
import com.etherblood.luna.network.api.game.messages.EventMessagePart;
import com.etherblood.luna.network.api.game.messages.LeaveGameRequest;
import com.etherblood.luna.network.api.game.messages.SpectateGameRequest;
import com.etherblood.luna.network.api.game.messages.SpectateGameResponse;
import com.etherblood.luna.network.api.game.messages.StartGameRequest;
import com.etherblood.luna.network.api.game.messages.UnspectateGameRequest;
import com.etherblood.luna.network.client.timestamp.TimestampClientModule;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.UUID;

public class GameClientModule extends GameModule {

    public static final int MILLIS_PER_SECOND = 1000;
    private final TimestampClientModule timestampModule;
    private final Connection connection;
    private ClientGame clientGame;
    private UUID spectatedGameId;

    public GameClientModule(TimestampClientModule timestampModule, Connection connection) {
        this.timestampModule = timestampModule;
        this.connection = connection;
    }

    public synchronized UUID start(String gameTemplate) {
        UUID gameId = UUID.randomUUID();
        connection.sendTCP(new StartGameRequest(gameId, GameRules.getDefault().getId(), gameTemplate));
        return gameId;
    }

    public synchronized void enter(String actorTemplate) {
        if (spectatedGameId != null) {
            connection.sendTCP(new EnterGameRequest(spectatedGameId, actorTemplate));
        }
    }

    public synchronized void leave() {
        if (spectatedGameId != null) {
            connection.sendTCP(new LeaveGameRequest(spectatedGameId));
        }
    }

    public synchronized void spectate(UUID gameId) {
        if (clientGame != null) {
            unspectate();
        }
        connection.sendTCP(new SpectateGameRequest(gameId));
        spectatedGameId = gameId;
    }

    public synchronized void unspectate() {
        if (spectatedGameId != null) {
            leave();
            connection.sendTCP(new UnspectateGameRequest(spectatedGameId));
            clientGame = null;
            spectatedGameId = null;
        }
    }

    @Override
    public synchronized void received(Connection connection, Object object) {
        if (object instanceof EventMessage message) {
            if (clientGame != null && clientGame.getBuilder().getSpectateId().equals(message.spectateId())) {
                ClientEventMessageBuilder builder = clientGame.getBuilder();
                PlaybackBuffer buffer = clientGame.getBuffer();
                GameEngine state = clientGame.getState();
                builder.updateAck(message);
                for (EventMessagePart part : message.parts()) {
                    buffer.buffer(part.frame(), part.event());
                }
                for (long frame = state.getFrame(); frame <= message.lockFrame(); frame++) {
                    state.tick(buffer.peek(frame));
                    buffer.lockFrame(frame);
                }
            }
        } else if (object instanceof SpectateGameResponse response) {
            clientGame = new ClientGame(response);
        }
    }

    public synchronized void run(GameEvent input) {
        if (clientGame != null) {
            ClientEventMessageBuilder builder = clientGame.getBuilder();
            GameEngine state = clientGame.getState();
            long nextFrame = (timestampModule.getApproxServerTime() - state.getStartEpochMillis()) * state.getRules().getFramesPerSecond() / MILLIS_PER_SECOND;
            if (clientGame.getServerFrame() < nextFrame) {
                do {
                    clientGame.incServerFrame();
                    builder.enqueueAction(new EventMessagePart(clientGame.getServerFrame(), input));
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
