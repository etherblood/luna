package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.font.BitmapText;
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
import com.etherblood.luna.application.client.text.EditableText;
import com.etherblood.luna.application.client.text.EditableTextWrapper;
import com.etherblood.luna.application.client.text.SelectionText;
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

    private final ClientChatModule chatModule;
    private final CommandService commandService;
    private final Consumer<ChatMessage> onMessage = this::onChatMessage;
    private final KeyListener onKey = this::onKey;
    private final CharacterListener onCharacter = this::onCharacter;
    private final MouseButtonListener onMouseButton = this::onMouseButton;
    private final MousePositionListener onMouseMove = this::onMouseMove;

    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private BitmapText chatTextNode;

    private boolean chatActive = false;
    private EditableTextWrapper wrapper;
    private EditableText editText;
    private Geometry backgroundQuad;

    private Vector2f selectionStart = null;

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

        chatTextNode = new BitmapText(application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt"), " ");
        chatTextNode.setLocalTranslation(new Vector3f(0, 70, 1));
        application.getGuiNode().add(chatTextNode);


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

        wrapper = new EditableTextWrapper(application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt"), material);
        editText = wrapper.getText();
        wrapper.getNode().setLocalTranslation(new Vector3f(1, 500, 0));


        Quad meshGround = new Quad(500, 500);

        Material materialQuad = new Material();
        materialQuad.setVertexShader(vertexShaderDefault);
        materialQuad.setFragmentShader(fragShaderDefault);
        materialQuad.getParameters().setVector4f("color", new Vector4f(0.05f, 0.05f, 0.05f, 1));
        materialQuad.setCullMode(VK10.VK_CULL_MODE_FRONT_BIT);
        materialQuad.setDepthTest(false);
        materialQuad.setDepthWrite(false);
        materialQuad.setTransparent(true);

        backgroundQuad = new Geometry();
        backgroundQuad.setMesh(meshGround);
        backgroundQuad.setMaterial(materialQuad);
        backgroundQuad.move(new Vector3f(0, 0, 0.5f));
        chatActive = false;
    }

    @Override
    public void update(int imageIndex, float tpf) {
        super.update(imageIndex, tpf);
        String text = messages.stream().map(m -> m.senderName() + ": " + Stream.of(m.message().split("\n")).collect(Collectors.joining(" "))).collect(Collectors.joining("\n"));
        if (text.isBlank()) {
            text = " ";
        }
        if (!text.equals(chatTextNode.getText())) {
            chatTextNode.setText(text);
        }

        wrapper.update();
    }

    @Override
    public void cleanupInternal() {
        super.cleanupInternal();
        chatModule.unsubscribe(onMessage);
        application.getInputManager().removeKeyListener(onKey);
        application.getInputManager().removeCharacterListener(onCharacter);
        application.getInputManager().removeMouseButtonListener(onMouseButton);
        application.getInputManager().removeMousePositionListener(onMouseMove);
        application.getGuiNode().remove(chatTextNode);
        application.getGuiNode().remove(wrapper.getNode());
        application.getGuiNode().remove(backgroundQuad);
    }

    private void onChatMessage(ChatMessage message) {
        if (messages.size() > 10) {
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
                application.getGuiNode().add(wrapper.getNode());
            }
            return;
        }

        if (keyEvent.getAction() == GLFW.GLFW_PRESS || keyEvent.getAction() == GLFW.GLFW_REPEAT) {
            boolean shift = (keyEvent.getModifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
            boolean ctrl = (keyEvent.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
            switch (keyEvent.getKey()) {
                case GLFW.GLFW_KEY_A:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        editText.push(editText.current().selectAll());
                    }
                    break;
                case GLFW.GLFW_KEY_V:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        String string = GLFW.glfwGetClipboardString(application.getWindow());
                        if (string != null) {
                            editText.push(editText.current().set(string));
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_C:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        String string = editText.current().selected();
                        if (!string.isEmpty()) {
                            GLFW.glfwSetClipboardString(application.getWindow(), string);
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_X:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        String string = editText.current().selected();
                        if (!string.isEmpty()) {
                            GLFW.glfwSetClipboardString(application.getWindow(), string);
                        }
                        editText.push(editText.current().set(""));
                    }
                    break;
                case GLFW.GLFW_KEY_Y:// german keyboard...
                case GLFW.GLFW_KEY_Z:
                    if (ctrl) {
                        if (shift) {
                            editText.redo();
                        } else {
                            editText.undo();
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_BACKSPACE:
                    editText.push(editText.current().deleteLeft(ctrl));
                    break;
                case GLFW.GLFW_KEY_DELETE:
                    editText.push(editText.current().deleteRight(ctrl));
                    break;
                case GLFW.GLFW_KEY_ENTER:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        String inputText = editText.current().text();
                        if (inputText.startsWith("/")) {
                            String result = commandService.runCommand(inputText.substring(1));
                            if (result != null) {
                                onChatMessage(new ChatMessage(0, "System", System.currentTimeMillis(), result));
                            }
                        } else if (!inputText.isEmpty()) {
                            chatModule.send(new ChatMessageRequest(inputText));
                        }
                        editText.push(SelectionText.empty());
                    }
                    break;
                case GLFW.GLFW_KEY_LEFT:
                    editText.push(editText.current().left(shift, ctrl));
                    break;
                case GLFW.GLFW_KEY_RIGHT:
                    editText.push(editText.current().right(shift, ctrl));
                    break;
                case GLFW.GLFW_KEY_HOME:
                    editText.push(editText.current().fullLeft(shift));
                    break;
                case GLFW.GLFW_KEY_END:
                    editText.push(editText.current().fullRight(shift));
                    break;
                case chatActivationKey:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        chatActive = false;
                        application.getGuiNode().remove(backgroundQuad);
                        application.getGuiNode().remove(wrapper.getNode());
                    }
                    break;
            }
        }
    }

    private void onCharacter(CharacterEvent event) {
        if (chatActive) {
            editText.push(editText.current().set(Character.toString(event.getCodepoint())));
        }
    }

    private void onMouseButton(MouseButtonEvent event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                selectionStart = new Vector2f(application.getInputManager().getCursorPosition());
                updateSelection();
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                updateSelection();
                selectionStart = null;
            }
        }
    }

    private void onMouseMove(MousePositionEvent event) {
        updateSelection();
    }

    private void updateSelection() {
        if (selectionStart != null) {
            Vector3f offset = wrapper.getNode().getLocalTransform().getTranslation();
            Vector2f selectionEnd = application.getInputManager().getCursorPosition();
            wrapper.setSelection(selectionStart.sub(offset.x(), offset.y(), new Vector2f()), selectionEnd.sub(offset.x(), offset.y(), new Vector2f()));
        }
    }
}
