package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.application.client.textbox.EditableTextbox;
import com.etherblood.luna.application.client.textbox.SelectionText;
import com.etherblood.luna.application.client.textbox.WindowClipboard;
import com.etherblood.luna.network.api.chat.ChatMessage;
import com.etherblood.luna.network.api.chat.ChatMessageRequest;
import com.etherblood.luna.network.client.chat.ClientChatModule;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;

public class ChatSystem extends LifecycleObject implements InputLayer {

    public static final int CHAT_LINES = 20;
    private final ClientChatModule chatModule;
    private final CommandService commandService;
    private final Consumer<ChatMessage> onMessage = this::onChatMessage;

    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    private EditableTextbox chatInput;
    private EditableTextbox chatDisplay;
    private Geometry backgroundQuad;
    private EditableTextbox focusedElement = null;

    public ChatSystem(ClientChatModule chatModule, CommandService commandService) {
        this.chatModule = chatModule;
        this.commandService = commandService;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.CHAT;
    }

    @Override
    public void init() {
        super.init();

        chatModule.subscribe(onMessage);

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

        WindowClipboard clipboard = new WindowClipboard(application.getWindow());
        BitmapFont font = application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt");
        chatDisplay = new EditableTextbox(font, material, clipboard, x -> {
        });
        chatDisplay.getText().setFreezeText(true);
        int topMargin = 100;
        chatDisplay.getNode().setLocalTranslation(new Vector3f(1, topMargin, 0));
        application.getGuiNode().add(chatDisplay.getNode());

        chatInput = new EditableTextbox(font, material, clipboard, inputText -> {
            if (inputText.startsWith("/")) {
                String result = commandService.runCommand(inputText.substring(1));
                if (result != null) {
                    onChatMessage(new ChatMessage(0, "System", System.currentTimeMillis(), result));
                }
            } else if (!inputText.isEmpty()) {
                chatModule.send(new ChatMessageRequest(inputText));
            }
        });
        chatInput.getNode().setLocalTranslation(new Vector3f(1, topMargin + CHAT_LINES * font.getLineHeight(), 0));

        Quad meshGround = new Quad(500, CHAT_LINES * font.getLineHeight());

        Material materialQuad = new Material();
        materialQuad.setVertexShader(vertexShaderDefault);
        materialQuad.setFragmentShader(fragShaderDefault);
        materialQuad.getParameters().setVector4f("color", new Vector4f(0.05f, 0.05f, 0.05f, 1));
        materialQuad.setCullMode(VK10.VK_CULL_MODE_FRONT_BIT);
        materialQuad.setDepthTest(false);
        materialQuad.setDepthWrite(false);

        backgroundQuad = new Geometry();
        backgroundQuad.setMesh(meshGround);
        backgroundQuad.setMaterial(materialQuad);
        backgroundQuad.setLocalTranslation(new Vector3f(0, topMargin, 0.1f));
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        String text = messages.stream()
                .map(m -> m.senderName() + ": " + Stream.of(m.message().split("\n"))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));

        if (!chatDisplay.getText().current().text().equals(text)) {
            chatDisplay.getText().setFreezeText(false);
            chatDisplay.getText().push(new SelectionText(text));
            chatDisplay.getText().setFreezeText(true);
        }

        chatDisplay.setFocus(chatDisplay == focusedElement);
        chatInput.setFocus(chatInput == focusedElement);

        chatInput.update();
        chatDisplay.update();
    }

    @Override
    public void cleanupInternal() {
        super.cleanupInternal();
        chatModule.unsubscribe(onMessage);
        application.getGuiNode().remove(chatDisplay.getNode());
        application.getGuiNode().remove(chatInput.getNode());
        application.getGuiNode().remove(backgroundQuad);
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
        if (focusedElement == null) {
            if (event.getKey() == chatActivationKey && event.getAction() == GLFW.GLFW_PRESS) {
                attach();
                return true;
            }
            return false;
        }

        focusedElement.onKey(event);
        if (event.getKey() == chatActivationKey && event.getAction() == GLFW.GLFW_PRESS) {
            detach();
        }
        return true;
    }

    private void attach() {
        application.getGuiNode().add(backgroundQuad);
        application.getGuiNode().add(chatInput.getNode());
        focusedElement = chatInput;
    }

    private void detach() {
        application.getGuiNode().remove(backgroundQuad);
        application.getGuiNode().remove(chatInput.getNode());
        focusedElement = null;
    }

    @Override
    public boolean consumeCharacter(CharacterEvent event) {
        if (focusedElement != null) {
            focusedElement.onCharacter(event);
            return true;
        }
        return false;
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        if (focusedElement != null) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                if (cursorPosition.y < chatInput.getNode().getLocalTransform().getTranslation().y) {
                    focusedElement = chatDisplay;
                } else {
                    focusedElement = chatInput;
                }
            }
            focusedElement.onMouseButton(event, cursorPosition);
            return true;
        }
        return false;
    }

    @Override
    public boolean consumeMouseMove(MousePositionEvent event) {
        if (focusedElement != null) {
            focusedElement.onMouseMove(event);
            return true;
        }
        return false;
    }
}
