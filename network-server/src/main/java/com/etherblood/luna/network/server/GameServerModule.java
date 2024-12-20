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
import com.etherblood.luna.network.server.lobby.LunaLobbyServerModule;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameServerModule extends GameModule {

    private static final long MILLIS_PER_SECOND = 1000;
    private final long clientDelayFrames = 2;
    private final StripedLock locking = new StripedLock(128);
    private final Map<Integer, Connection> connections = new ConcurrentHashMap<>();
    private final JwtServerModule jwtModule;
    private final LunaLobbyServerModule lobbyModule;

    private final Map<UUID, ServerGame> games = new ConcurrentHashMap<>();

    public GameServerModule(JwtServerModule jwtModule, LunaLobbyServerModule lobbyModule) {
        this.jwtModule = jwtModule;
        this.lobbyModule = lobbyModule;
        GameEngine lobbyGame = new GameEngine(GameModule.LOBBY_GAME_ID, GameRules.getDefault(), System.currentTimeMillis(), 0);
        lobbyGame.applyTemplate(lobbyGame.getData().createEntity(), "lobby_room");
        games.put(GameModule.LOBBY_GAME_ID, new ServerGame(lobbyGame));
        lobbyModule.listGame(lobbyGame, "lobby_room");
    }

    @Override
    public void connected(Connection connection) {
        connections.put(connection.getID(), connection);
    }

    @Override
    public void disconnected(Connection connection) {
        for (ServerGame serverGame : games.values()) {
            if (serverGame.getBuilders().remove(connection.getID()) != null) {
                JwtAuthenticationUser user = jwtModule.getUser(connection.getID());
                EventMessagePart part = new EventMessagePart(serverGame.getState().getFrame(), new GameEvent(null, new PlayerJoined(user.id, user.login, null, false)));
                broadcast(serverGame, part);
            }
        }
        connections.remove(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof EventMessage message) {
            locking.runSynchronized(message.spectateId(), () -> {
                for (ServerGame serverGame : games.values()) {
                    // TODO: permission checks?
                    Map<Integer, ServerEventMessageBuilder> builders = serverGame.getBuilders();
                    ServerEventMessageBuilder builder = builders.get(connection.getID());
                    if (builder != null && builder.getSpectateId().equals(message.spectateId())) {
                        builder.updateAck(message);
                        PlaybackBuffer buffer = serverGame.getBuffer();
                        for (EventMessagePart part : message.parts()) {
                            long frame = part.frame() + clientDelayFrames;
                            buffer.buffer(frame, part.event());
                        }
                    }
                }
            });
        } else if (object instanceof SpectateGameRequest request) {
            locking.runSynchronized(request.gameId(), () -> {
                // TODO: permission checks?
                JwtAuthenticationUser user = jwtModule.getUser(connection.getID());
                ServerGame serverGame = games.get(request.gameId());
                GameEngine state = serverGame.getState();
                Map<Integer, ServerEventMessageBuilder> builders = serverGame.getBuilders();

                System.out.println("User " + user.login + " spectates game " + state.getId());
                UUID spectateId = UUID.randomUUID();
                builders.put(connection.getID(), new ServerEventMessageBuilder(spectateId));
                connection.sendTCP(new SpectateGameResponse(spectateId, state));
            });
        } else if (object instanceof UnspectateGameRequest request) {
            locking.runSynchronized(request.gameId(), () -> {
                // TODO: permission checks?
                JwtAuthenticationUser user = jwtModule.getUser(connection.getID());
                ServerGame serverGame = games.get(request.gameId());
                GameEngine state = serverGame.getState();
                Map<Integer, ServerEventMessageBuilder> builders = serverGame.getBuilders();
                builders.remove(connection.getID());
                System.out.println("User " + user.login + " unspectates game " + state.getId());
            });
        } else if (object instanceof EnterGameRequest request) {
            locking.runSynchronized(request.gameId(), () -> {
                // TODO: permission checks?
                JwtAuthenticationUser user = jwtModule.getUser(connection.getID());
                ServerGame serverGame = games.get(request.gameId());
                EventMessagePart part = new EventMessagePart(serverGame.getState().getFrame(), new GameEvent(null, new PlayerJoined(user.id, user.login, request.actorTemplate(), true)));
                broadcast(serverGame, part);
                System.out.println("User " + user.login + " enters game " + request.gameId() + " with " + request.actorTemplate());
            });
        } else if (object instanceof LeaveGameRequest request) {
            locking.runSynchronized(request.gameId(), () -> {
                // TODO: permission checks?
                JwtAuthenticationUser user = jwtModule.getUser(connection.getID());
                ServerGame serverGame = games.get(request.gameId());
                EventMessagePart part = new EventMessagePart(serverGame.getState().getFrame(), new GameEvent(null, new PlayerJoined(user.id, user.login, null, false)));
                broadcast(serverGame, part);
                System.out.println("User " + user.login + " leaves game " + request.gameId());
            });
        } else if (object instanceof StartGameRequest request) {
            locking.runSynchronized(request.gameId(), () -> {
                // TODO: permission checks?
                GameRules rules = GameRules.get(request.gameRules());
                GameEngine game = new GameEngine(request.gameId(), rules, System.currentTimeMillis(), new EntityDataImpl(rules.getComponentTypes()), 0);
                game.applyTemplate(game.getData().createEntity(), request.gameTemplate());
                lobbyModule.listGame(game, request.gameTemplate());
                if (games.putIfAbsent(request.gameId(), new ServerGame(game)) == null) {
                    System.out.println("Started " + request.gameTemplate() + " " + request.gameId());
                }
            });
        }
    }

    private void broadcast(ServerGame serverGame, EventMessagePart part) {
        PlaybackBuffer buffer = serverGame.getBuffer();
        if (!buffer.buffer(part.frame(), part.event())) {
            throw new IllegalStateException("broadcast of " + part + " failed.");
        }
    }

    public void update() {
        games.values().parallelStream().forEach(serverGame
                -> locking.runSynchronized(serverGame.getState().getId(), () -> {
            GameEngine state = serverGame.getState();
            PlaybackBuffer buffer = serverGame.getBuffer();
            Map<Integer, ServerEventMessageBuilder> builders = serverGame.getBuilders();

            long servertime = System.currentTimeMillis();
            long nextFrame = (servertime - state.getStartEpochMillis()) * state.getRules().getFramesPerSecond() / MILLIS_PER_SECOND;
            if (state.getFrame() < nextFrame) {
                do {
                    long frame = state.getFrame();
                    Set<GameEvent> events = buffer.peek(frame);
                    buffer.lockFrame(frame);
                    state.tick(events);

                    for (ServerEventMessageBuilder builder : builders.values()) {
                        for (GameEvent event : events) {
                            builder.broadcast(new EventMessagePart(frame, event));
                        }
                    }
                } while (state.getFrame() < nextFrame);

                for (Map.Entry<Integer, ServerEventMessageBuilder> entry : builders.entrySet()) {
                    Connection connection = connections.get(entry.getKey());
                    ServerEventMessageBuilder builder = entry.getValue();
                    builder.lockFrame(buffer.getLockedFrame());
                    connection.sendUDP(builder.build());
                }

                lobbyModule.update(state);
            }
        }));
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
