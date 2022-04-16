package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.gametools.network.client.modules.jwt.JwtClientModule;
import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.KeyListener;
import com.destrostudios.icetea.core.system.AppSystem;
import com.etherblood.luna.network.api.chat.ChatMessage;
import com.etherblood.luna.network.client.chat.ClientChatModule;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class ChatSystem extends AppSystem {

    private final ClientChatModule chatModule;
    private final JwtClientModule jwtModule;
    private final Consumer<ChatMessage> onMessage = this::onChatMessage;
    private final KeyListener onKey = this::onKey;
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private BitmapText chatText;

    public ChatSystem(ClientChatModule chatModule, JwtClientModule jwtModule) {
        this.chatModule = chatModule;
        this.jwtModule = jwtModule;
    }

    @Override
    public void initialize(Application application) {
        super.initialize(application);
        chatModule.subscribe(onMessage);
        application.getInputManager().addKeyListener(onKey);
        chatText = new BitmapText(application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt"), "-");
        chatText.setLocalTranslation(new Vector3f(0, 70, 1));
        application.getGuiNode().add(chatText);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        String text = messages.stream().map(m -> m.senderName() + ": " + m.message()).collect(Collectors.joining("\n"));
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
                            JwtAuthenticationUser user = jwtModule.getOwnAuthentication().user;
                            chatModule.send(new ChatMessage(user.id, user.login, string));
                        }
                    }
                    break;
            }
        }
    }
}
