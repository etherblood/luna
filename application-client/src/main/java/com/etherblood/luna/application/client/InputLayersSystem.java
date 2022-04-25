package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.CharacterListener;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.KeyListener;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MouseButtonListener;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.input.MousePositionListener;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class InputLayersSystem extends LifecycleObject {

    private final KeyListener onKey = this::onKey;
    private final CharacterListener onCharacter = this::onCharacter;
    private final MouseButtonListener onMouseButton = this::onMouseButton;
    private final MousePositionListener onMouseMove = this::onMouseMove;

    private final Map<Integer, InputLayer> keyFocus = new HashMap<>();
    private final Map<Integer, InputLayer> mouseFocus = new HashMap<>();

    @Override
    protected void init() {
        super.init();
        application.getInputManager().addKeyListener(onKey);
        application.getInputManager().addCharacterListener(onCharacter);
        application.getInputManager().addMouseButtonListener(onMouseButton);
        application.getInputManager().addMousePositionListener(onMouseMove);
    }

    @Override
    protected void cleanupInternal() {
        super.cleanupInternal();
        application.getInputManager().removeKeyListener(onKey);
        application.getInputManager().removeCharacterListener(onCharacter);
        application.getInputManager().removeMouseButtonListener(onMouseButton);
        application.getInputManager().removeMousePositionListener(onMouseMove);
    }

    private void onKey(KeyEvent event) {
        InputLayer focus = null;
        for (InputLayer layer : layers(keyFocus.get(event.getKey()))) {
            if (layer.consumeKey(event)) {
                focus = layer;
                break;
            }
        }
        updateFocus(keyFocus, focus, event.getKey(), event.getAction());
    }

    private void onCharacter(CharacterEvent event) {
        // TODO: how to handle focus?
        for (InputLayer layer : layers(keyFocus.values())) {
            if (layer.consumeCharacter(event)) {
                return;
            }
        }
    }

    private void onMouseButton(MouseButtonEvent event) {
        Vector2f cursorPosition = new Vector2f(application.getInputManager().getCursorPosition());
        InputLayer focus = null;
        for (InputLayer layer : layers(mouseFocus.get(event.getButton()))) {
            if (layer.consumeMouseButton(event, cursorPosition)) {
                focus = layer;
                break;
            }
        }
        updateFocus(mouseFocus, focus, event.getButton(), event.getAction());
    }

    private void onMouseMove(MousePositionEvent event) {
        for (InputLayer layer : layers(mouseFocus.values())) {
            if (layer.consumeMouseMove(event)) {
                return;
            }
        }
    }

    private static void updateFocus(Map<Integer, InputLayer> focusMap, InputLayer layer, int key, int action) {
        if (action == GLFW.GLFW_PRESS) {
            if (layer != null) {
                focusMap.put(key, layer);
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            focusMap.remove(key);
        }
    }

    private List<InputLayer> layers(Collection<InputLayer> forcedFocus) {
        if (forcedFocus.isEmpty()) {
            layers((InputLayer) null);
        }
        return forcedFocus.stream()
                .sorted(Comparator.comparingInt(InputLayer::orderNumber))
                .toList();
    }

    private List<InputLayer> layers(InputLayer forcedFocus) {
        if (forcedFocus != null) {
            return List.of(forcedFocus);
        }
        return application.getSystems().stream()
                .filter(InputLayer.class::isInstance)
                .map(InputLayer.class::cast)
                .sorted(Comparator.comparingInt(InputLayer::orderNumber))
                .toList();
    }
}
