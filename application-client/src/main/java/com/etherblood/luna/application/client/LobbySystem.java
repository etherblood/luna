package com.etherblood.luna.application.client;

import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.gametools.network.client.modules.jwt.JwtClientModule;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.application.client.listbox.Listbox;
import com.etherblood.luna.network.api.lobby.LobbyInfo;
import com.etherblood.luna.network.api.lobby.Player;
import com.etherblood.luna.network.client.GameClientModule;
import com.etherblood.luna.network.client.timestamp.TimestampClientModule;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;

public class LobbySystem extends LifecycleObject implements InputLayer {

    private static final UUID START_GAME_ID = UUID.randomUUID();
    public static final String LOBBY_TEMPLATE = "lobby_room";
    private final LobbyClientModule<LobbyInfo> lobbyModule;
    private final GameClientModule gameModule;
    private final TimestampClientModule timestamps;
    private final JwtClientModule jwtModule;
    private Listbox<String> templates;
    private Listbox<UUID> games;
    private Listbox<?> focused;

    public LobbySystem(LobbyClientModule<LobbyInfo> lobbyModule, GameClientModule gameModule, TimestampClientModule timestamps, JwtClientModule jwtModule) {
        this.lobbyModule = lobbyModule;
        this.gameModule = gameModule;
        this.timestamps = timestamps;
        this.jwtModule = jwtModule;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.LOBBY;
    }

    @Override
    protected void init() {
        super.init();

        BitmapFont font = application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt");

        Shader vertexShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });
        Shader fragShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });

        Material material = new Material();
        material.setVertexShader(vertexShaderDefault);
        material.setFragmentShader(fragShaderDefault);
        material.setCullMode(VK10.VK_CULL_MODE_NONE);
        material.getParameters().setVector4f("color", new Vector4f(11 / 255f, 104 / 255f, 217 / 255f, 1));
        material.setDepthTest(false);
        material.setDepthWrite(false);

        Mesh quad = new Quad(1, 1);
        Geometry g = new Geometry();
        g.setMesh(quad);
        g.setMaterial(material);
        g.setShadowMode(ShadowMode.OFF);
        Geometry g2 = new Geometry();
        g2.setMesh(quad);
        g2.setMaterial(material);
        g2.setShadowMode(ShadowMode.OFF);

        lobbyModule.subscribeToGamesList();
        templates = new Listbox<>(font, x -> x, g);
        templates.setList(List.of(LOBBY_TEMPLATE, "test_room", "challenge_room"));
        games = new Listbox<>(font, id -> {
            if (id.equals(START_GAME_ID)) {
                return "new game";
            }
            LobbyInfo info = lobbyModule.getListedGames().get(id);
            return durationToString(Duration.ofMillis(timestamps.getApproxServerTime() - info.startEpochMillis()))
                    + ": "
                    + info.players().stream().map(Player::name).collect(Collectors.joining(", "));
        },
                g2);
        templates.getNode().setLocalTranslation(new Vector3f(0, 100, 0));
        games.getNode().setLocalTranslation(new Vector3f(300, 100, 0));

        application.getGuiNode().add(templates.getNode());
        application.getGuiNode().add(games.getNode());

        focused = templates;
    }

    @Override
    protected void cleanupInternal() {
        super.cleanupInternal();
        lobbyModule.unsubscribeFromGamesList();
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        String selectedTemplate = templates.getSelected();
        Map<UUID, LobbyInfo> listedGames = lobbyModule.getListedGames();
        List<UUID> list = listedGames.values().stream()
                .filter(x -> x.gameTemplate().equals(selectedTemplate))
                .sorted(Comparator.comparingLong(LobbyInfo::startEpochMillis).thenComparing(x -> x.gameId()))
                .map(LobbyInfo::gameId)
                .collect(Collectors.toCollection(ArrayList::new));
        if (templates.getSelected() != null && !templates.getSelected().equals(LOBBY_TEMPLATE)) {
            list.add(START_GAME_ID);
        }
        games.setList(list);

        templates.update();
        games.update();
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        if (focused != null) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                switch (event.getKey()) {
                    case GLFW.GLFW_KEY_ENTER -> onConfirm();
                    case GLFW.GLFW_KEY_LEFT -> {
                        focused = templates;
                        games.setSelected(null);
                    }
                    case GLFW.GLFW_KEY_RIGHT -> focused = games;
                    default -> focused.onKey(event);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            if (cursorPosition.x < games.getNode().getLocalTransform().getTranslation().x) {
                focused = templates;
                games.setSelected(null);
            } else {
                focused = games;
            }
        }
        Vector3f translation = focused.getNode().getLocalTransform().getTranslation();
        focused.onMouseButton(event, cursorPosition.sub(new Vector2f(translation.x, translation.y), new Vector2f()));
        return true;
    }

    private void onConfirm() {
        gameModule.leave();
        UUID selected = games.getSelected();
        if (START_GAME_ID.equals(selected)) {
            selected = gameModule.start(templates.getSelected());
        }
        gameModule.spectate(selected);
        gameModule.enter("amara");
    }

    private static String durationToString(Duration duration) {
        StringBuilder builder = new StringBuilder();
        long days = duration.toDays();
        if (days > 1) {
            builder.append(days);
            builder.append(" days ");
        } else if (days == 1) {
            builder.append("1 day ");
        }
        if (duration.toHours() > 0) {
            builder.append(duration.toHoursPart());
            builder.append(" h ");
        }
        if (duration.toMinutes() > 0) {
            builder.append(duration.toMinutesPart());
            builder.append(" min ");
        }
        int seconds = duration.toSecondsPart();
        builder.append(seconds);
        builder.append(" s");
        return builder.toString();
    }
}
