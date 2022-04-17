package com.etherblood.luna.network.server.chat;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.gametools.network.server.modules.jwt.JwtServerModule;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.network.api.chat.ChatMessage;
import com.etherblood.luna.network.api.chat.ChatMessageRequest;
import com.etherblood.luna.network.api.chat.ChatModule;
import java.util.function.Supplier;

public class ServerChatModule extends ChatModule {

    private final JwtServerModule jwtModule;
    private final Supplier<Connection[]> connectionsSupply;

    public ServerChatModule(JwtServerModule jwtModule, Supplier<Connection[]> connectionsSupply) {
        this.jwtModule = jwtModule;
        this.connectionsSupply = connectionsSupply;
    }


    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ChatMessageRequest message) {
            for (Connection other : connectionsSupply.get()) {
                JwtAuthenticationUser user = jwtModule.getUser(connection.getID());
                other.sendTCP(new ChatMessage(user.id, user.login, System.currentTimeMillis(), message.message()));
            }
        }
    }
}
