package com.etherblood.luna.application.client;

import java.io.IOException;

public class LocalhostMain1 {

    public static void main(String... args) throws IOException {
        Main.startApp("localhost", Main.getTestJwt(1), true);
    }
}
