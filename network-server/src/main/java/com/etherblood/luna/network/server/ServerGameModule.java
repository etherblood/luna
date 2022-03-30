package com.etherblood.luna.network.server;

import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import com.etherblood.luna.network.api.GameModule;
import com.etherblood.luna.network.api.PlaybackBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGameModule extends GameModule {

    private final GameEngine state;
    private final Map<Integer, ServerEventMessageBuilder> builders = new ConcurrentHashMap<>();
    private final Map<Integer, Connection> connections = new ConcurrentHashMap<>();
    private final PlaybackBuffer buffer = new PlaybackBuffer();

    public ServerGameModule(GameEngine state) {
        this.state = state;
    }

    @Override
    public void connected(Connection connection) {
        connection.sendTCP(state);
        builders.put(connection.getID(), new ServerEventMessageBuilder());
        connections.put(connection.getID(), connection);
    }

    @Override
    public void disconnected(Connection connection) {
        builders.remove(connection.getID());
        connections.remove(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof EventMessage message) {
            ServerEventMessageBuilder builder = builders.get(connection.getID());
            builder.updateAck(message);
            for (EventMessagePart part : message.parts()) {
                if (buffer.buffer(part.frame(), part.event())) {
                    for (ServerEventMessageBuilder other : builders.values()) {
                        other.broadcast(new EventMessagePart(part.frame(), part.event()));
                    }
                }
            }
        }
    }

    public void tick() {
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
