package com.etherblood.luna.network.api;

import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.Movebox;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Speed;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.network.api.serialization.EnumSerializer;
import com.etherblood.luna.network.api.serialization.EventMessageSerializer;
import com.etherblood.luna.network.api.serialization.GameEngineSerializer;
import com.etherblood.luna.network.api.serialization.RecordSerializer;

public abstract class GameModule extends NetworkModule {
    @Override
    public void initialize(Kryo kryo) {
        kryo.register(GameEngine.class, new GameEngineSerializer());
        kryo.register(EventMessage.class, new EventMessageSerializer());
        kryo.register(GameEvent.class, new RecordSerializer<GameEvent>());
        kryo.register(PlayerInput.class, new RecordSerializer<PlayerInput>());

        kryo.register(OwnedBy.class, new RecordSerializer<>());
        kryo.register(Position.class, new RecordSerializer<>());
        kryo.register(Speed.class, new RecordSerializer<>());
        kryo.register(Movebox.class, new RecordSerializer<>());
        kryo.register(ActorState.class, new RecordSerializer<>());
        kryo.register(Direction.class, new EnumSerializer<>(Direction.class));
//        kryo.register(PlayerAction.class, new EnumSerializer<>(PlayerAction.class));

        kryo.register(Rectangle.class, new RecordSerializer<>());
        kryo.register(Vector2.class, new RecordSerializer<>());
        kryo.register(ActorAction.class, new EnumSerializer(ActorAction.class));
    }
}
