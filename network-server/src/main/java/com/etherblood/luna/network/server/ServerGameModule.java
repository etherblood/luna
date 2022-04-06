package com.etherblood.luna.network.server;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Login;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerJoined;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import com.etherblood.luna.network.api.GameModule;
import com.etherblood.luna.network.api.PlaybackBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGameModule extends GameModule {

    private final Object lock = new Object();
    private final GameEngine state;
    private final Map<Integer, ServerEventMessageBuilder> builders = new ConcurrentHashMap<>();
    private final Map<Integer, Connection> connections = new ConcurrentHashMap<>();
    private final PlaybackBuffer buffer = new PlaybackBuffer();

    public ServerGameModule(GameEngine state) {
        this.state = state;
    }

    @Override
    public void connected(Connection connection) {
    }

    @Override
    public void disconnected(Connection connection) {
        builders.remove(connection.getID());
        connections.remove(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof EventMessage message) {
            synchronized (lock) {
                ServerEventMessageBuilder builder = builders.get(connection.getID());
                builder.updateAck(message);
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
        if (object instanceof Login login) {
            JwtAuthenticationUser user = new NoValidateJwtService().decode(login.jwt).user;
            synchronized (lock) {
                long frame = state.getFrame();
                long targetFrame = frame + 1;

                System.out.println("User " + user.login + " connected on frame " + frame + " join scheduled for frame " + targetFrame);
                connection.sendTCP(state);
                builders.put(connection.getID(), new ServerEventMessageBuilder());
                connections.put(connection.getID(), connection);

                GameEvent event = new GameEvent(null, new PlayerJoined(user.id, user.login, true));
                buffer.buffer(targetFrame, event);
                for (ServerEventMessageBuilder builder : builders.values()) {
                    builder.broadcast(new EventMessagePart(targetFrame, event));
                }
            }
        }
    }

    public void update() {
        synchronized (lock) {
            long frame = state.getFrame();
            Set<GameEvent> events = buffer.peek(frame);
            buffer.clear(frame);
            state.tick(events);
            for (Map.Entry<Integer, ServerEventMessageBuilder> entry : builders.entrySet()) {
                Connection connection = connections.get(entry.getKey());
                ServerEventMessageBuilder builder = builders.get(entry.getKey());
                builder.lockFrame(frame);
                connection.sendUDP(builder.build());
            }
        }
    }
}
