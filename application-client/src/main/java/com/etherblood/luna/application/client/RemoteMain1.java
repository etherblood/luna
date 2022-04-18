package com.etherblood.luna.application.client;

import com.destrostudios.gametools.network.client.ToolsClient;
import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.gametools.network.client.modules.jwt.JwtClientModule;
import com.etherblood.luna.network.client.GameClientModule;
import com.etherblood.luna.network.client.chat.ClientChatModule;
import com.etherblood.luna.network.client.timestamp.TimestampClientModule;
import java.io.IOException;

public class RemoteMain1 {

    public static void main(String... args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        ToolsClient toolsClient = Main.createToolsClient();
        System.out.println("created client");
        ApplicationClient app = new ApplicationClient(Main.remoteProxy(toolsClient, "localhost", Main.getTestJwt(1))) {
            @Override
            protected void init() {
                super.init();
                CommandService commandService = new CommandService(
                        toolsClient.getModule(JwtClientModule.class),
                        toolsClient.getModule(TimestampClientModule.class),
                        toolsClient.getModule(GameClientModule.class),
                        toolsClient.getModule(LobbyClientModule.class));
                addSystem(new ChatSystem(toolsClient.getModule(ClientChatModule.class), commandService));
            }
        };
        System.out.println("starting app...");
        app.start();
    }
}
