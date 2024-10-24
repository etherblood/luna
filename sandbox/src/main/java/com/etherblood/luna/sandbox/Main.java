package com.etherblood.luna.sandbox;

import com.destrostudios.authtoken.NoValidateJwtService;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.application.server.AppServer;

import java.io.IOException;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
//        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs

        AppServer server = new AppServer(new NoValidateJwtService());
        server.start();
        com.etherblood.luna.application.client.Main.startApp("localhost", com.etherblood.luna.application.client.Main.getTestJwt(1), false);
        server.stop();
    }


}