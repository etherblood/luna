package com.etherblood.luna.network.server.timestamp;

import com.esotericsoftware.kryonet.Server;
import com.etherblood.luna.network.api.NetworkUtil;
import java.io.IOException;

public class TimestampServerMain {

    public static void main(String... args) throws InterruptedException, IOException {
        Server server = new Server(10_000, 10_000);
        TimestampServerModule module = new TimestampServerModule();
        module.initialize(server.getKryo());
        server.addListener(module);

        server.start();
        server.bind(NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);
        while (true) {
            Thread.sleep(1000);
        }
    }
}
