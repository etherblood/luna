package com.etherblood.luna.network.server.chat;

import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.network.api.chat.ChatMessage;
import com.etherblood.luna.network.api.chat.ChatModule;
import java.util.function.Supplier;

public class ServerChatModule extends ChatModule {

    private final Supplier<Connection[]> connectionsSupply;

    public ServerChatModule(Supplier<Connection[]> connectionsSupply) {
        this.connectionsSupply = connectionsSupply;
    }


    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ChatMessage message) {
            for (Connection other : connectionsSupply.get()) {
                other.sendTCP(message);
            }
        }
    }
}
