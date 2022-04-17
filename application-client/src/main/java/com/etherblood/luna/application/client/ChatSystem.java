package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.KeyListener;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.etherblood.luna.network.api.chat.ChatMessage;
import com.etherblood.luna.network.api.chat.ChatMessageRequest;
import com.etherblood.luna.network.client.chat.ClientChatModule;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class ChatSystem extends LifecycleObject {

    private final ClientChatModule chatModule;
    private final Consumer<ChatMessage> onMessage = this::onChatMessage;
    private final KeyListener onKey = this::onKey;
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private BitmapText chatText;

    public ChatSystem(ClientChatModule chatModule) {
        this.chatModule = chatModule;
    }

    @Override
    public void init(Application application) {
        super.init(application);
        chatModule.subscribe(onMessage);
        application.getInputManager().addKeyListener(onKey);
        chatText = new BitmapText(application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt"), "-");
        chatText.setLocalTranslation(new Vector3f(0, 70, 1));
        application.getGuiNode().add(chatText);
    }

    @Override
    public void update(Application application, int imageIndex, float tpf) {
        super.update(application, imageIndex, tpf);
        String text = messages.stream().map(m -> m.senderName() + ": " + Stream.of(m.message().split("\n")).collect(Collectors.joining(" "))).collect(Collectors.joining("\n"));
        if (!text.isBlank() && !text.equals(chatText.getText())) {
            chatText.setText(text);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        chatModule.unsubscribe(onMessage);
        application.getInputManager().removeKeyListener(onKey);
        application.getGuiNode().remove(chatText);
    }

    private void onChatMessage(ChatMessage message) {
        if (messages.size() > 10) {
            messages.remove(0);
        }
        messages.add(message);
    }

    private void onKey(KeyEvent keyEvent) {
        if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
            switch (keyEvent.getKey()) {
                case GLFW.GLFW_KEY_V:
                    if ((keyEvent.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
                        String string = GLFW.glfwGetClipboardString(application.getWindow());
                        if (string != null) {
                            if (string.startsWith("/")) {
                                // TODO: command magic
                            } else {
                                chatModule.send(new ChatMessageRequest(string));
                            }
                        }
                    }
                    break;
            }
        }
    }
}
