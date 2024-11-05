package com.etherblood.luna.network.api;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatsSerialization extends KryoSerialization {
    public final Map<String, StatsRecord> reads = new ConcurrentHashMap<>();
    public final Map<String, StatsRecord> writes = new ConcurrentHashMap<>();

    @Override
    public synchronized void write(Connection connection, ByteBuffer buffer, Object object) {
        super.write(connection, buffer, object);
        record(writes, object.getClass().getSimpleName(), buffer.position());
    }

    @Override
    public synchronized Object read(Connection connection, ByteBuffer buffer) {
        int bytes = buffer.position();
        Object object = super.read(connection, buffer);
        record(reads, object.getClass().getSimpleName(), bytes);
        return object;
    }

    @Override
    public void writeLength(ByteBuffer buffer, int length) {
        super.writeLength(buffer, length);
        record(writes, "length", getLengthLength());
    }

    @Override
    public int readLength(ByteBuffer buffer) {
        record(reads, "length", getLengthLength());
        int length = super.readLength(buffer);
        return length;
    }

    private static void record(Map<String, StatsRecord> map, String type, long bytes) {
        map.compute(type, (t, prev) -> {
            if (prev == null) {
                return new StatsRecord(1, bytes, bytes, bytes);
            }
            return new StatsRecord(prev.count() + 1, Math.min(prev.minBytes(), bytes), Math.max(prev.maxBytes(), bytes), prev.totalBytes() + bytes);
        });
    }
}
