package com.etherblood.luna.application.client;

import java.io.IOException;

public class RemoteMain2 {

    public static void main(String... args) throws IOException {
        new ApplicationClient(Main.remoteProxy(2)).start();
    }
}
