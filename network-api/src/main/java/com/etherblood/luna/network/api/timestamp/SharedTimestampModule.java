package com.etherblood.luna.network.api.timestamp;

import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.network.api.NetworkModule;
import com.etherblood.luna.network.api.serialization.RecordSerializer;

public class SharedTimestampModule extends NetworkModule {
    @Override
    public void initialize(Kryo kryo) {
        kryo.register(TimestampPing.class, new RecordSerializer<TimestampPing>());
        kryo.register(TimestampPong.class, new RecordSerializer<TimestampPong>());
    }
}
