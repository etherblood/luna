package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.gametools.network.client.modules.jwt.JwtClientModule;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerJoined;
import com.etherblood.luna.network.api.game.GameModule;
import com.etherblood.luna.network.api.lobby.LobbyInfo;
import com.etherblood.luna.network.client.GameClientModule;
import com.etherblood.luna.network.client.timestamp.TimestampClientModule;
import java.util.Locale;
import java.util.UUID;

public class CommandService {

    private final JwtClientModule jwtModule;
    private final TimestampClientModule timestampModule;
    private final GameClientModule gameModule;
    private final LobbyClientModule<LobbyInfo> lobbyModule;
    private UUID selectedGameId = GameModule.LOBBY_GAME_ID;

    public CommandService(JwtClientModule jwtModule, TimestampClientModule timestampModule, GameClientModule gameModule, LobbyClientModule<LobbyInfo> lobbyModule) {
        this.jwtModule = jwtModule;
        this.timestampModule = timestampModule;
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
                case "spectate": {
                    if (parts.length >= 2) {
                        selectedGameId = UUID.fromString(parts[1]);
                    }
                    gameModule.spectate(selectedGameId);
                    break;
                }
                case "enter": {
                    long approxServerTime = timestampModule.getApproxServerTime();
                    JwtAuthenticationUser user = jwtModule.getOwnAuthentication().user;
                    gameModule.run(approxServerTime, new GameEvent(null, new PlayerJoined(user.id, user.login, parts[1], true)));
                    break;
                }
                case "leave": {
                    long approxServerTime = timestampModule.getApproxServerTime();
                    JwtAuthenticationUser user = jwtModule.getOwnAuthentication().user;
                    gameModule.run(approxServerTime, new GameEvent(null, new PlayerJoined(user.id, user.login, null, false)));
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
                default: {
                    System.out.println("Command not found: " + rawCommand);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to handle command: " + rawCommand);
            e.printStackTrace();
        }
    }
}
