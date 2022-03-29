package com.etherblood.luna.network.api.timestamp;

import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.network.api.serialization.RecordSerializer;

public class SharedTimestampModule extends NetworkModule {
    @Override
    public void initialize(Kryo kryo) {
        kryo.register(TimestampPing.class, new RecordSerializer<>());
        kryo.register(TimestampPong.class, new RecordSerializer<>());
    }
}
