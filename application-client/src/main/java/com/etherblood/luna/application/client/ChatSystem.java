package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.CharacterListener;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.KeyListener;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MouseButtonListener;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.input.MousePositionListener;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.application.client.text.EditableTextbox;
import com.etherblood.luna.application.client.text.SelectionText;
import com.etherblood.luna.application.client.text.WindowClipboard;
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

public class ChatSystem extends LifecycleObject {

    public static final int CHAT_LINES = 20;
    private final ClientChatModule chatModule;
    private final CommandService commandService;
    private final Consumer<ChatMessage> onMessage = this::onChatMessage;
    private final KeyListener onKey = this::onKey;
    private final CharacterListener onCharacter = this::onCharacter;
    private final MouseButtonListener onMouseButton = this::onMouseButton;
    private final MousePositionListener onMouseMove = this::onMouseMove;

    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    private boolean chatActive = false;
    private EditableTextbox chatInput;
    private EditableTextbox chatDisplay;
    private Geometry backgroundQuad;

    private EditableTextbox focusedElement;

    public ChatSystem(ClientChatModule chatModule, CommandService commandService) {
        this.chatModule = chatModule;
        this.commandService = commandService;
    }

    @Override
    public void init() {
        super.init();

        chatModule.subscribe(onMessage);
        application.getInputManager().addKeyListener(onKey);
        application.getInputManager().addCharacterListener(onCharacter);
        application.getInputManager().addMouseButtonListener(onMouseButton);
        application.getInputManager().addMousePositionListener(onMouseMove);

        // Ground
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
        chatActive = false;

        focusedElement = chatInput;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        String text = messages.stream()
                .map(m -> m.senderName() + ": " + Stream.of(m.message().split("\n"))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));

        if (!chatDisplay.getText().current().text().equals(text)) {
            chatDisplay.getText().push(new SelectionText(text));
        }

        chatDisplay.setShowSelection(chatDisplay == focusedElement);
        chatInput.setShowSelection(chatInput == focusedElement);

        chatInput.update();
        chatDisplay.update();
    }

    @Override
    public void cleanupInternal() {
        super.cleanupInternal();
        chatModule.unsubscribe(onMessage);
        application.getInputManager().removeKeyListener(onKey);
        application.getInputManager().removeCharacterListener(onCharacter);
        application.getInputManager().removeMouseButtonListener(onMouseButton);
        application.getInputManager().removeMousePositionListener(onMouseMove);
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

    private void onKey(KeyEvent keyEvent) {
        final int chatActivationKey = GLFW.GLFW_KEY_ESCAPE;
        if (!chatActive) {
            if (keyEvent.getKey() == chatActivationKey && keyEvent.getAction() == GLFW.GLFW_PRESS) {
                chatActive = true;
                application.getGuiNode().add(backgroundQuad);
                application.getGuiNode().add(chatInput.getNode());
            }
            return;
        }

        if (keyEvent.getAction() == GLFW.GLFW_PRESS || keyEvent.getAction() == GLFW.GLFW_REPEAT) {
            switch (keyEvent.getKey()) {
                case chatActivationKey:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        chatActive = false;
                        application.getGuiNode().remove(backgroundQuad);
                        application.getGuiNode().remove(chatInput.getNode());
                    }
                    break;
                default:
                    focusedElement.onKey(keyEvent);
                    break;
            }
        }
    }

    private void onCharacter(CharacterEvent event) {
        if (chatActive) {
            focusedElement.onCharacter(event);
        }
    }

    private void onMouseButton(MouseButtonEvent event) {
        if (chatActive) {
            Vector2f cursorPosition = new Vector2f(application.getInputManager().getCursorPosition());
            if (event.getAction() == GLFW.GLFW_PRESS) {
                if (cursorPosition.y < chatInput.getNode().getLocalTransform().getTranslation().y) {
                    focusedElement = chatDisplay;
                } else {
                    focusedElement = chatInput;
                }
            }
            focusedElement.onMouseButton(event, cursorPosition);
        }
    }

    private void onMouseMove(MousePositionEvent event) {
        if (chatActive) {
            focusedElement.onMouseMove(event);
        }
    }
}
