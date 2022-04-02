package com.etherblood.luna.application.client;

import java.io.IOException;

public class RemoteMain2 {

    public static void main(String... args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        new ApplicationClient(Main.remoteProxy("localhost", Main.getTestJwt(2))).start();
    }
}
