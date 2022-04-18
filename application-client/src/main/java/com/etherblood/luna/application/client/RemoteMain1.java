package com.etherblood.luna.application.client;

import com.destrostudios.gametools.network.client.ToolsClient;
import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.etherblood.luna.network.client.ClientGameModule;
import com.etherblood.luna.network.client.chat.ClientChatModule;
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
                CommandService commandService = new CommandService(toolsClient.getModule(ClientGameModule.class), toolsClient.getModule(LobbyClientModule.class));
                addSystem(new ChatSystem(toolsClient.getModule(ClientChatModule.class), commandService));
            }
        };
        System.out.println("starting app...");
        app.start();
    }
}
