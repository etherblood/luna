package com.etherblood.luna.network.client.timestamp;

import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.network.api.timestamp.SharedTimestampModule;
import com.etherblood.luna.network.api.timestamp.TimestampPing;
import com.etherblood.luna.network.api.timestamp.TimestampPong;

public class ClientTimestampModule extends SharedTimestampModule {

    private final Connection connection;
    private final Object lock = new Object();
    private final long[] deltas;
    private final int intervalMillis;
    private int deltaCount = 0;
    private long requestId = 0;
    private long requestMillis = 0;
    private boolean pending = false;
    private long latency = 0;

    public ClientTimestampModule(Connection connection, int bufferSize, int intervalMillis) {
        this.connection = connection;
        this.deltas = new long[bufferSize];
        this.intervalMillis = intervalMillis;
    }

    /**
     * this should be called regularly, it will ping the server if required
     */
    public void run() {
        if (requestMillis + intervalMillis <= System.currentTimeMillis()) {
            pending = true;
            requestMillis = System.currentTimeMillis();
            requestId++;
            connection.sendUDP(new TimestampPing(requestId));
        }
    }

    public boolean isInitialized() {
        synchronized (lock) {
            return deltaCount >= deltas.length;
        }
    }

    public long getApproxServerTime() {
        synchronized (lock) {
            int count = Math.min(deltaCount, deltas.length);
            if (count == 0) {
                return System.currentTimeMillis();
            }
            long sum = 0;
            for (int i = 0; i < count; i++) {
                sum += deltas[i];
            }
            return Math.floorDiv(sum, count) + System.currentTimeMillis();
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof TimestampPong pong) {
            if (pending && pong.requestId() == requestId) {
                pending = false;
                long currentTimeMillis = System.currentTimeMillis();
                long delta = pong.timestamp() - (currentTimeMillis + requestMillis) / 2;
                synchronized (lock) {
                    deltas[deltaCount % deltas.length] = delta;
                    deltaCount++;
                    latency = currentTimeMillis - requestMillis;
                }
            }
        }
    }

    public long getLatency() {
        return latency;
    }
}
