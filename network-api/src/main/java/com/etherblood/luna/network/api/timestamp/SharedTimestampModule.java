package com.etherblood.luna.network.api.timestamp;

import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.esotericsoftware.kryo.Kryo;

public class SharedTimestampModule extends NetworkModule {
    @Override
    public void initialize(Kryo kryo) {
        kryo.register(TimestampPing.class, new RecordSerializer<>());
        kryo.register(TimestampPong.class, new RecordSerializer<>());
    }
}
