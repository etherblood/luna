package com.etherblood.luna.network.api.lobby;

import com.destrostudios.gametools.network.shared.serializers.CopySerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface SharedLunaLobbyModule {
    static void initialize(Kryo kryo) {
        kryo.register(LobbyInfo.class, new CopySerializer<LobbyInfo>() {
            @Override
            public void write(Kryo kryo, Output output, LobbyInfo object) {
                kryo.writeObject(output, object.gameId());
                output.writeLong(object.startEpochMillis());
                output.writeString(object.gameTemplate());
                output.writeInt(object.players().size());
                for (Player player : object.players()) {
                    output.writeLong(player.id());
                    output.writeString(player.name());
                }
            }

            @Override
            public LobbyInfo read(Kryo kryo, Input input, Class<LobbyInfo> type) {
                UUID id = kryo.readObject(input, UUID.class);
                long millis = input.readLong();
                String template = input.readString();
                int length = input.readInt();
                List<Player> list = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    Player player = new Player(input.readLong(), input.readString());
                    list.add(player);
                }
                return new LobbyInfo(id, millis, template, list);
            }
        });
    }
}
