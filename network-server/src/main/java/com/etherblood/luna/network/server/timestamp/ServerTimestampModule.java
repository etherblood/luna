package com.etherblood.luna.network.server.timestamp;

import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.network.api.timestamp.SharedTimestampModule;
import com.etherblood.luna.network.api.timestamp.TimestampPing;
import com.etherblood.luna.network.api.timestamp.TimestampPong;

public class ServerTimestampModule extends SharedTimestampModule {

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof TimestampPing ping) {
            connection.sendUDP(new TimestampPong(ping.requestId(), System.currentTimeMillis()));
        }
    }
}
