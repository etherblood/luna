package com.etherblood.luna.application.client;

import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.etherblood.luna.network.api.game.GameModule;
import com.etherblood.luna.network.api.lobby.LobbyInfo;
import com.etherblood.luna.network.client.ClientGameModule;
import java.util.Locale;
import java.util.UUID;

public class CommandService {

    private final ClientGameModule gameModule;
    private final LobbyClientModule<LobbyInfo> lobbyModule;
    private UUID selectedGameId = GameModule.LOBBY_GAME_ID;

    public CommandService(ClientGameModule gameModule, LobbyClientModule<LobbyInfo> lobbyModule) {
        this.gameModule = gameModule;
        this.lobbyModule = lobbyModule;
    }

    public void runCommand(String rawCommand) {
        if (rawCommand.isBlank()) {
            return;
        }
        try {
            String[] parts = rawCommand.split(" ");
            switch (parts[0].toLowerCase(Locale.ROOT)) {
                case "start": {
                    selectedGameId = gameModule.start(parts[1]);
                    break;
                }
                case "join": {
                    gameModule.join(selectedGameId, parts[1]);
                    break;
                }
                case "select": {
                    if (parts.length >= 2) {
                        selectedGameId = UUID.fromString(parts[1]);
                    } else {
                        selectedGameId = GameModule.LOBBY_GAME_ID;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to handle command: " + rawCommand);
            e.printStackTrace();
        }
    }
}
