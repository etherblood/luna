package com.etherblood.luna.network.client.timestamp;

import com.esotericsoftware.kryonet.Client;
import com.etherblood.luna.network.api.NetworkUtil;
import java.io.IOException;

public class TimestampClientMain {
    public static void main(String... args) throws InterruptedException, IOException {
        Client client = new Client(10_000, 10_000);
        ClientTimestampModule module = new ClientTimestampModule(client, 10, 500);
        module.initialize(client.getKryo());
        client.addListener(module);

        client.start();
        client.connect(10_000, "localhost", NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);
        while (true) {
            module.run();
            Thread.sleep(1000);
            System.out.println("approx:" + module.getApproxServerTime());
            System.out.println("actual:" + System.currentTimeMillis());
        }
    }
}
