package com.etherblood.luna.application.client;

import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.application.client.gui.Button;
import com.etherblood.luna.application.client.gui.InputLayer;
import com.etherblood.luna.application.client.gui.LayerOrder;
import com.etherblood.luna.application.client.gui.Listbox;
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
    private Listbox<?> focused;
    private Listbox<String> templates;
    private Listbox<UUID> games;
    private Button confirmButton;

    public LobbySystem(LobbyClientModule<LobbyInfo> lobbyModule, GameClientModule gameModule, TimestampClientModule timestamps) {
        this.lobbyModule = lobbyModule;
        this.gameModule = gameModule;
        this.timestamps = timestamps;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.LOBBY;
    }

    private boolean isAttached() {
        return templates.getNode().hasParent(application.getGuiNode());
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


        Material material2 = new Material();
        material2.setVertexShader(vertexShaderDefault);
        material2.setFragmentShader(fragShaderDefault);
        material2.setCullMode(VK10.VK_CULL_MODE_NONE);
        material2.getParameters().setVector4f("color", new Vector4f(0.5f, 0.5f, 0.5f, 1));
        material2.setDepthTest(false);
        material2.setDepthWrite(false);
        Geometry g3 = new Geometry();
        g3.setMesh(quad);
        g3.setMaterial(material2);
        g3.setShadowMode(ShadowMode.OFF);
        confirmButton = new Button(g3, new BitmapText(font, "confirm"));
        confirmButton.setDimensions(new Vector2f(800, 100), new Vector2f(200, 80));

        focused = templates;
    }

    private void attach() {
        application.getGuiNode().add(templates.getNode());
        application.getGuiNode().add(games.getNode());
        application.getGuiNode().add(confirmButton.getNode());
    }

    @Override
    protected void cleanupInternal() {
        super.cleanupInternal();
        lobbyModule.unsubscribeFromGamesList();
    }

    private void detach() {
        application.getGuiNode().remove(templates.getNode());
        application.getGuiNode().remove(games.getNode());
        application.getGuiNode().remove(confirmButton.getNode());
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
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
            if (isAttached()) {
                detach();
            } else {
                attach();
            }
            return true;
        }
        if (isAttached() && focused != null) {
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
        if (!isAttached()) {
            return false;
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            if (confirmButton.contains(cursorPosition)) {
                if (onConfirm()) {
                    return true;
                }
            }
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

    private boolean onConfirm() {
        UUID selectedGame = games.getSelected();
        if (games == focused && selectedGame != null) {
            gameModule.leave();
            if (START_GAME_ID.equals(selectedGame)) {
                selectedGame = gameModule.start(templates.getSelected());
            }
            gameModule.spectate(selectedGame);
            gameModule.enter("amara");
            detach();
            return true;
        }
        return false;
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
