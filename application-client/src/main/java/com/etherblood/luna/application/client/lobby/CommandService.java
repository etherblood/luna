package com.etherblood.luna.application.client.lobby;

import com.etherblood.luna.network.api.game.GameModule;
import com.etherblood.luna.network.client.GameClientModule;

import java.util.Locale;
import java.util.UUID;

public class CommandService {

    private final Runnable exit;
    private final GameClientModule gameModule;
    private UUID selectedGameId = GameModule.LOBBY_GAME_ID;

    public CommandService(Runnable exit, GameClientModule gameModule) {
        this.exit = exit;
        this.gameModule = gameModule;
    }

    public String runCommand(String rawCommand) {
        if (rawCommand.isBlank()) {
            return null;
        }
        try {
            String[] parts = rawCommand.split(" ");
            switch (parts[0].toLowerCase(Locale.ROOT)) {
                case "start" -> {
                    selectedGameId = gameModule.start(parts[1]);
                    return selectedGameId.toString();
                }
                case "spectate" -> {
                    if (parts.length >= 2) {
                        selectedGameId = UUID.fromString(parts[1]);
                    }
                    gameModule.spectate(selectedGameId);
                    return selectedGameId.toString();
                }
                case "unspectate" -> {
                    gameModule.unspectate();
                    return null;
                }
                case "enter" -> {
                    gameModule.enter(parts[1]);
                    return null;
                }
                case "leave" -> {
                    gameModule.leave();
                    return null;
                }
                case "select" -> {
                    if (parts.length >= 2) {
                        selectedGameId = UUID.fromString(parts[1]);
                    } else {
                        selectedGameId = GameModule.LOBBY_GAME_ID;
                    }
                    return selectedGameId.toString();
                }
                case "selected" -> {
                    return selectedGameId.toString();
                }
                case "exit" -> {
                    exit.run();
                    return "EXIT";
                }
                default -> {
                    return "Command not found: " + rawCommand;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to handle command: " + rawCommand;
        }
    }
}
