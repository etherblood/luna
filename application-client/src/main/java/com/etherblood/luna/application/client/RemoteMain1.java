package com.etherblood.luna.application.client;

import java.io.IOException;

public class RemoteMain1 {

    public static void main(String... args) throws IOException {
        new ApplicationClient(Main.remoteProxy("localhost", Main.getTestJwt(1))).start();
    }
}