package com.etherblood.luna.network.client.chat;

import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.network.api.chat.ChatMessage;
import com.etherblood.luna.network.api.chat.ChatMessageRequest;
import com.etherblood.luna.network.api.chat.ChatModule;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientChatModule extends ChatModule {

    private final Connection connection;
    private final List<Consumer<ChatMessage>> listeners = new ArrayList<>();

    public ClientChatModule(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ChatMessage message) {
            for (Consumer<ChatMessage> listener : listeners) {
                listener.accept(message);
            }
        }
    }

    public void send(ChatMessageRequest message) {
        connection.sendTCP(message);
    }

    public void subscribe(Consumer<ChatMessage> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<ChatMessage> listener) {
        listeners.remove(listener);
    }
}
