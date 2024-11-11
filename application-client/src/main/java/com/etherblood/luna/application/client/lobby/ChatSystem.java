package com.etherblood.luna.application.client.lobby;

import com.destrostudios.icetea.core.AppSystem;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.etherblood.luna.application.client.gui.BoundingRectangle;
import com.etherblood.luna.application.client.gui.GuiContainer;
import com.etherblood.luna.application.client.gui.GuiFactory;
import com.etherblood.luna.application.client.gui.GuiManager;
import com.etherblood.luna.application.client.gui.InputLayer;
import com.etherblood.luna.application.client.gui.LayerOrder;
import com.etherblood.luna.application.client.gui.textbox.EditableTextbox;
import com.etherblood.luna.application.client.gui.textbox.SelectionText;
import com.etherblood.luna.network.api.chat.ChatMessage;
import com.etherblood.luna.network.api.chat.ChatMessageRequest;
import com.etherblood.luna.network.client.chat.ClientChatModule;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChatSystem extends AppSystem implements InputLayer {

    public static final int CHAT_LINES = 20;
    private final ClientChatModule chatModule;
    private final CommandService commandService;
    private final GuiManager guiManager;
    private final GuiFactory guiFactory;
    private GuiContainer container;
    private final Consumer<ChatMessage> onMessage = this::onChatMessage;

    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    private EditableTextbox chatInput;
    private EditableTextbox chatDisplay;

    public ChatSystem(ClientChatModule chatModule, CommandService commandService, GuiManager guiManager, GuiFactory guiFactory) {
        this.chatModule = chatModule;
        this.commandService = commandService;
        this.guiManager = guiManager;
        this.guiFactory = guiFactory;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.CHAT;
    }

    @Override
    public void onAttached() {
        super.onAttached();

        chatModule.subscribe(onMessage);

        chatDisplay = guiFactory.editableTextbox(new BoundingRectangle(0, 0, 300, CHAT_LINES * guiFactory.bitmapFont().getLineHeight()), x -> {
        });
        chatDisplay.getText().setFreezeText(true);
        application.getGuiNode().add(chatDisplay.node());

        chatInput = guiFactory.editableTextbox(new BoundingRectangle(0, CHAT_LINES * guiFactory.bitmapFont().getLineHeight(), 300, guiFactory.bitmapFont().getLineHeight()), inputText -> {
            if (inputText.startsWith("/")) {
                String result = commandService.runCommand(inputText.substring(1));
                if (result != null) {
                    onChatMessage(new ChatMessage(0, "System", System.currentTimeMillis(), result));
                }
            } else if (!inputText.isEmpty()) {
                chatModule.send(new ChatMessageRequest(inputText));
            }
            detach();
        });

        container = guiFactory.container(new BoundingRectangle(0, 100, 300, (CHAT_LINES + 1) * guiFactory.bitmapFont().getLineHeight()));
        container.add(chatDisplay);
        container.add(chatInput);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (!isAttached()) {
            return;
        }
        String text = messages.stream()
                .map(m -> m.senderName() + ": " + String.join(" ", m.message().split("\n")))
                .collect(Collectors.joining("\n"));

        if (!chatDisplay.getText().current().text().equals(text)) {
            chatDisplay.setSelectionText(new SelectionText(text));
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        chatModule.unsubscribe(onMessage);
        guiManager.getRootContainer().remove(container);
    }

    private void onChatMessage(ChatMessage message) {
        if (messages.size() >= CHAT_LINES) {
            messages.remove(0);
        }
        messages.add(message);
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        final int chatActivationKey = GLFW.GLFW_KEY_ENTER;
        if (event.getKey() == chatActivationKey && event.getAction() == GLFW.GLFW_PRESS) {
            if (isAttached()) {
                detach();
            } else {
                attach();
            }
            return true;
        }
        return container.isFocused();
    }

    private boolean isAttached() {
        return guiManager.getRootContainer().getChilds().contains(container);
    }

    private void attach() {
        guiManager.getRootContainer().add(container);
        guiManager.getRootContainer().setFocusedChild(container);
        container.setFocusedChild(chatInput);
    }

    private void detach() {
        guiManager.getRootContainer().remove(container);
    }

}
