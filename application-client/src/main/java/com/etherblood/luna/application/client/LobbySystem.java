package com.etherblood.luna.application.client;

import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.icetea.core.AppSystem;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.etherblood.luna.application.client.gui.BoundingRectangle;
import com.etherblood.luna.application.client.gui.Button;
import com.etherblood.luna.application.client.gui.GuiContainer;
import com.etherblood.luna.application.client.gui.GuiFactory;
import com.etherblood.luna.application.client.gui.GuiManager;
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
import org.lwjgl.glfw.GLFW;

public class LobbySystem extends AppSystem implements InputLayer {

    private static final UUID START_GAME_ID = UUID.randomUUID();
    public static final String LOBBY_TEMPLATE = "lobby_room";
    private final LobbyClientModule<LobbyInfo> lobbyModule;
    private final GameClientModule gameModule;
    private final TimestampClientModule timestamps;
    private final GuiManager guiManager;
    private final GuiFactory guiFactory;
    private GuiContainer container;
    private Listbox<String> templates;
    private Listbox<UUID> games;
    private Button confirmButton;

    public LobbySystem(LobbyClientModule<LobbyInfo> lobbyModule, GameClientModule gameModule, TimestampClientModule timestamps, GuiManager guiManager, GuiFactory guiFactory) {
        this.lobbyModule = lobbyModule;
        this.gameModule = gameModule;
        this.timestamps = timestamps;
        this.guiManager = guiManager;
        this.guiFactory = guiFactory;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.LOBBY;
    }

    private boolean isAttached() {
        return guiManager.getRootContainer().getChilds().contains(container);
    }

    @Override
    public void onAttached() {
        super.onAttached();

        lobbyModule.subscribeToGamesList();
        templates = guiFactory.listbox(new BoundingRectangle(0, 0, 300, 600), x -> x);
        templates.setList(List.of(LOBBY_TEMPLATE, "test_room", "challenge_room"));
        games = guiFactory.listbox(new BoundingRectangle(300, 0, 300, 600), id -> {
            if (id.equals(START_GAME_ID)) {
                return "new game";
            }
            LobbyInfo info = lobbyModule.getListedGames().get(id);
            return durationToString(Duration.ofMillis(timestamps.getApproxServerTime() - info.startEpochMillis()))
                    + ": "
                    + info.players().stream().map(Player::name).collect(Collectors.joining(", "));
        });

        confirmButton = guiFactory.button(new BoundingRectangle(600, 0, 200, 80), this::onConfirm);
        confirmButton.setText("confirm");

        container = guiFactory.container(new BoundingRectangle(300, 100, 800, 600));
        container.add(templates);
        container.add(games);
        container.add(confirmButton);

        container.setFocusedChild(templates);
    }

    private void attach() {
        guiManager.getRootContainer().add(container);
        guiManager.getRootContainer().setFocusedChild(container);
    }

    @Override
    public void onDetached() {
        super.onDetached();
        lobbyModule.unsubscribeFromGamesList();
    }

    private void detach() {
        guiManager.getRootContainer().remove(container);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (!isAttached()) {
            return;
        }
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
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
            if (isAttached()) {
                detach();
            } else {
                attach();
            }
        }
        if (isAttached() && container.getFocusedChild() != null) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                switch (event.getKey()) {
                    case GLFW.GLFW_KEY_ENTER -> onConfirm();
                }
            }
        }
        return container.isFocused();
    }

    private boolean onConfirm() {
        UUID selectedGame = games.getSelected();
        if (selectedGame != null) {
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
