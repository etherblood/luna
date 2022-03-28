package com.etherblood.lunia.application.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameService {

    private final Map<UUID, GameData> games = new ConcurrentHashMap<>();


    public void tick() {
        
    }

}
