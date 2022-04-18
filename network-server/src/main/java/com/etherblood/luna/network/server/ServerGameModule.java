package com.etherblood.luna.network.server;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.gametools.network.server.modules.jwt.JwtServerModule;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.data.EntityDataImpl;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.PlayerJoined;
import com.etherblood.luna.network.api.game.EventMessage;
import com.etherblood.luna.network.api.game.EventMessagePart;
import com.etherblood.luna.network.api.game.GameModule;
import com.etherblood.luna.network.api.game.JoinRequest;
import com.etherblood.luna.network.api.game.PlaybackBuffer;
import com.etherblood.luna.network.api.game.StartGameRequest;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGameModule extends GameModule {

    private static final long MILLIS_PER_SECOND = 1000;
    private final Object lock = new Object();
    private final Map<Integer, Connection> connections = new ConcurrentHashMap<>();
    private final JwtServerModule jwtModule;

    private final Map<UUID, ServerGame> games = new HashMap<>();

    public ServerGameModule(JwtServerModule jwtModule) {
        this.jwtModule = jwtModule;
        GameEngine lobbyGame = new GameEngine(GameModule.LOBBY_GAME_ID, GameRules.getDefault(), System.currentTimeMillis(), 0);
        lobbyGame.applyTemplate(lobbyGame.getData().createEntity(), "lobby_room");
        games.put(GameModule.LOBBY_GAME_ID, new ServerGame(lobbyGame));
    }

    @Override
    public void connected(Connection connection) {
        connections.put(connection.getID(), connection);
    }

    @Override
    public void disconnected(Connection connection) {
        for (ServerGame serverGame : games.values()) {
            serverGame.getBuilders().remove(connection.getID());
        }
        connections.remove(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof EventMessage message) {
            synchronized (lock) {
                ServerGame serverGame = games.get(message.gameId());
                if (serverGame != null) {
                    Map<Integer, ServerEventMessageBuilder> builders = serverGame.getBuilders();
                    ServerEventMessageBuilder builder = builders.get(connection.getID());
                    if (builder != null) {
                        builder.updateAck(message);
                    }
                    PlaybackBuffer buffer = serverGame.getBuffer();
                    for (EventMessagePart part : message.parts()) {
                        if (buffer.buffer(part.frame(), part.event())) {
                            for (ServerEventMessageBuilder other : builders.values()) {
                                other.broadcast(new EventMessagePart(part.frame(), part.event()));
                            }
                        } else {
                            // this is likely a duplicate of already handled input, do nothing
                        }
                    }
                }
            }
        } else if (object instanceof StartGameRequest request) {
            synchronized (lock) {
                GameRules rules = GameRules.get(request.gameRules());
                GameEngine game = new GameEngine(request.gameId(), rules, System.currentTimeMillis(), new EntityDataImpl(rules.getComponentTypes()), 0);
                game.applyTemplate(game.getData().createEntity(), request.gameTemplate());
                if (games.putIfAbsent(request.gameId(), new ServerGame(game)) == null) {
                    System.out.println("Started " + request.gameTemplate() + " " + request.gameId());
                }
            }
        } else if (object instanceof JoinRequest request) {
            synchronized (lock) {
                JwtAuthenticationUser user = jwtModule.getUser(connection.getID());

                // leave previous game
                for (ServerGame other : games.values()) {
                    Map<Integer, ServerEventMessageBuilder> builders = other.getBuilders();
                    if (builders.containsKey(connection.getID())) {
                        GameEngine state = other.getState();
                        builders.remove(connection.getID());
                        PlaybackBuffer buffer = other.getBuffer();
                        long leaveFrame = state.getFrame() + 1;
                        GameEvent event = new GameEvent(null, new PlayerJoined(user.id, user.login, null, false));
                        for (ServerEventMessageBuilder builder : builders.values()) {
                            if (!builder.broadcast(new EventMessagePart(leaveFrame, event))) {
                                throw new IllegalStateException("Failed to broadcast leave.");
                            }
                        }
                        if (!buffer.buffer(leaveFrame, event)) {
                            throw new IllegalStateException("Failed to buffer leave.");
                        }
                        System.out.println("User " + user.login + " left " + state.getId() + " on frame " + leaveFrame);
                    }
                }

                // join game
                ServerGame serverGame = games.get(request.gameId());
                GameEngine state = serverGame.getState();
                Map<Integer, ServerEventMessageBuilder> builders = serverGame.getBuilders();
                PlaybackBuffer buffer = serverGame.getBuffer();

                long joinFrame = state.getFrame() + 1;
                System.out.println("User " + user.login + " connected to " + serverGame.getState().getId() + " on frame " + joinFrame);
                connection.sendTCP(state);
                builders.put(connection.getID(), new ServerEventMessageBuilder(state.getId()));

                GameEvent event = new GameEvent(null, new PlayerJoined(user.id, user.login, request.actorTemplate(), true));
                for (ServerEventMessageBuilder builder : builders.values()) {
                    if (!builder.broadcast(new EventMessagePart(joinFrame, event))) {
                        throw new IllegalStateException("Failed to broadcast join.");
                    }
                }
                if (!buffer.buffer(joinFrame, event)) {
                    throw new IllegalStateException("Failed to buffer join.");
                }
            }
        }
    }

    public void update() {
        synchronized (lock) {
            for (ServerGame serverGame : games.values()) {
                GameEngine state = serverGame.getState();
                PlaybackBuffer buffer = serverGame.getBuffer();
                Map<Integer, ServerEventMessageBuilder> builders = serverGame.getBuilders();

                long servertime = System.currentTimeMillis();
                long nextFrame = (servertime - state.getStartEpochMillis()) * state.getRules().getFramesPerSecond() / MILLIS_PER_SECOND;
                if (state.getFrame() < nextFrame) {
                    do {
                        long frame = state.getFrame();
                        Set<GameEvent> events = buffer.peek(frame);
                        buffer.clear(frame);
                        state.tick(events);
                    } while (state.getFrame() < nextFrame);

                    for (Map.Entry<Integer, ServerEventMessageBuilder> entry : builders.entrySet()) {
                        Connection connection = connections.get(entry.getKey());
                        ServerEventMessageBuilder builder = builders.get(entry.getKey());
                        builder.lockFrame(nextFrame);
                        connection.sendUDP(builder.build());
                    }
                }
            }
        }
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
