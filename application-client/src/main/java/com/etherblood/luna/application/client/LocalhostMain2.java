package com.etherblood.luna.application.client;

import java.io.IOException;

public class LocalhostMain2 {

    public static void main(String... args) throws IOException {
        Main.startApp("localhost", Main.getTestJwt(2), true);
    }
}
