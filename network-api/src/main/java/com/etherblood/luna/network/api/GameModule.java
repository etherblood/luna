package com.etherblood.luna.network.api;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Login;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogin;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogout;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.engine.ActorInput;
import com.etherblood.luna.engine.ActorName;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Circle;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.ModelKey;
import com.etherblood.luna.engine.PendingDelete;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.PlayerJoined;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.SkillSet;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.ActionKey;
import com.etherblood.luna.engine.actions.Attack1Cooldown;
import com.etherblood.luna.engine.actions.Attack2Cooldown;
import com.etherblood.luna.engine.behaviors.GhostBehavior;
import com.etherblood.luna.engine.damage.DamageTrigger;
import com.etherblood.luna.engine.damage.Damagebox;
import com.etherblood.luna.engine.damage.Hitbox;
import com.etherblood.luna.engine.damage.MilliHealth;
import com.etherblood.luna.engine.damage.Team;
import com.etherblood.luna.engine.movement.Movebox;
import com.etherblood.luna.engine.movement.Obstaclebox;
import com.etherblood.luna.engine.movement.Speed;
import com.etherblood.luna.network.api.serialization.EnumSerializer;
import com.etherblood.luna.network.api.serialization.EventMessageSerializer;
import com.etherblood.luna.network.api.serialization.GameEngineSerializer;
import com.etherblood.luna.network.api.serialization.RecordSerializer;
import java.util.EnumMap;

public abstract class GameModule extends NetworkModule {
    @Override
    public void initialize(Kryo kryo) {
        kryo.register(GameEngine.class, new GameEngineSerializer());
        kryo.register(EventMessage.class, new EventMessageSerializer());
        kryo.register(GameEvent.class, new RecordSerializer<>());
        kryo.register(PlayerInput.class, new RecordSerializer<>());

        kryo.register(ActorInput.class, new RecordSerializer<>());
        kryo.register(Position.class, new RecordSerializer<>());
        kryo.register(Speed.class, new RecordSerializer<>());
        kryo.register(Movebox.class, new RecordSerializer<>());
        kryo.register(Obstaclebox.class, new RecordSerializer<>());
        kryo.register(Hitbox.class, new RecordSerializer<>());
        kryo.register(Damagebox.class, new RecordSerializer<>());
        kryo.register(DamageTrigger.class, new EnumSerializer<>(DamageTrigger.class));
        kryo.register(ActorState.class, new RecordSerializer<>());
        kryo.register(MilliHealth.class, new RecordSerializer<>());
        kryo.register(ModelKey.class, new RecordSerializer<>());
        kryo.register(PendingDelete.class, new RecordSerializer<>());
        kryo.register(Team.class, new RecordSerializer<>());
        kryo.register(GhostBehavior.class, new RecordSerializer<>());
        kryo.register(Attack1Cooldown.class, new RecordSerializer<>());
        kryo.register(Attack2Cooldown.class, new RecordSerializer<>());

        kryo.register(Rectangle.class, new RecordSerializer<>());
        kryo.register(Circle.class, new RecordSerializer<>());
        kryo.register(Vector2.class, new RecordSerializer<>());
        kryo.register(ActionKey.class, new EnumSerializer<>(ActionKey.class));
        kryo.register(Direction.class, new RecordSerializer<>());

        kryo.register(PlayerId.class, new RecordSerializer<>());
        kryo.register(ActorName.class, new RecordSerializer<>());
        kryo.register(PlayerJoined.class, new RecordSerializer<>());

        kryo.register(SkillSet.class, new Serializer<SkillSet>() {
            @Override
            public void write(Kryo kryo, Output output, SkillSet object) {
                for (ActionKey key : ActionKey.values()) {
                    output.writeString(object.skillMap().get(key));
                }
            }

            @Override
            public SkillSet read(Kryo kryo, Input input, Class<SkillSet> type) {
                EnumMap<ActionKey, String> map = new EnumMap<>(ActionKey.class);
                for (ActionKey key : ActionKey.values()) {
                    String value = input.readString();
                    if (value != null) {
                        map.put(key, value);
                    }
                }
                return new SkillSet(map);
            }
        });

        // workaround for broken default serializers
        // TODO: move elsewhere (or better: fix library)
        kryo.register(Login.class, new Serializer<Login>() {
            @Override
            public void write(Kryo kryo, Output output, Login object) {
                output.writeString(object.jwt);
            }

            @Override
            public Login read(Kryo kryo, Input input, Class<Login> type) {
                return new Login(input.readString());
            }
        });

        kryo.register(UserLogin.class, new Serializer<UserLogin>() {
            @Override
            public void write(Kryo kryo, Output output, UserLogin object) {
                kryo.writeObject(output, object.user);
            }

            @Override
            public UserLogin read(Kryo kryo, Input input, Class<UserLogin> type) {
                return new UserLogin(kryo.readObject(input, JwtAuthenticationUser.class));
            }
        });
        kryo.register(UserLogout.class, new Serializer<UserLogout>() {
            @Override
            public void write(Kryo kryo, Output output, UserLogout object) {
                kryo.writeObject(output, object.user);
            }

            @Override
            public UserLogout read(Kryo kryo, Input input, Class<UserLogout> type) {
                return new UserLogout(kryo.readObject(input, JwtAuthenticationUser.class));
            }
        });

        kryo.register(JwtAuthenticationUser.class, new Serializer<JwtAuthenticationUser>() {
            @Override
            public void write(Kryo kryo, Output output, JwtAuthenticationUser object) {
                output.writeLong(object.id);
                output.writeString(object.login);
            }

            @Override
            public JwtAuthenticationUser read(Kryo kryo, Input input, Class<JwtAuthenticationUser> type) {
                return new JwtAuthenticationUser(input.readLong(), input.readString());
            }
        });
    }
}
