package com.etherblood.luna.network.api.chat;

import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.esotericsoftware.kryo.Kryo;

public class ChatModule extends NetworkModule {

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(ChatMessage.class, new RecordSerializer<>());
        kryo.register(ChatMessageRequest.class, new RecordSerializer<>());
    }

}
