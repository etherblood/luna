package com.etherblood.luna.network.client.lobby;

import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.network.api.lobby.LobbyInfo;
import com.etherblood.luna.network.api.lobby.SharedLunaLobbyModule;

public class LunaLobbyClientModule extends LobbyClientModule<LobbyInfo> {
    public LunaLobbyClientModule(Connection connection) {
        super(SharedLunaLobbyModule::initialize, connection);
    }
}
