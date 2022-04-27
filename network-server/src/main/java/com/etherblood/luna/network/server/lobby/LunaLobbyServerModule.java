package com.etherblood.luna.network.server.lobby;

import com.destrostudios.gametools.network.server.modules.game.LobbyServerModule;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorName;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.network.api.lobby.LobbyInfo;
import com.etherblood.luna.network.api.lobby.Player;
import com.etherblood.luna.network.api.lobby.SharedLunaLobbyModule;
import java.util.Comparator;
import java.util.function.Supplier;

public class LunaLobbyServerModule extends LobbyServerModule<LobbyInfo> {

    public LunaLobbyServerModule(Supplier<Connection[]> connectionsSupply) {
        super(SharedLunaLobbyModule::initialize, connectionsSupply);
    }

    public void listGame(GameEngine game, String template) {
        listGame(game.getId(), createLobbyInfo(game, template));
    }

    public void update(GameEngine game) {
        LobbyInfo info = getGames().get(game.getId());
        LobbyInfo nextInfo = createLobbyInfo(game, info.gameTemplate());
        if (!info.equals(nextInfo)) {
            listGame(game, info.gameTemplate());
        }
    }

    private LobbyInfo createLobbyInfo(GameEngine game, String template) {
        EntityData data = game.getData();
        return new LobbyInfo(
                game.getId(),
                game.getStartEpochMillis(),
                template,
                data.list(PlayerId.class).stream()
                        .map(entity -> {
                            long id = data.get(entity, PlayerId.class).id();
                            ActorName actorName = data.get(entity, ActorName.class);
                            String name = actorName == null ? "#" + id : actorName.name();
                            return new Player(id, name);
                        })
                        .distinct()
                        .sorted(Comparator.comparing(Player::name).thenComparingLong(Player::id))
                        .toList()
        );
    }

}
