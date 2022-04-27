package com.etherblood.luna.application.client.listbox;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Listbox<T> {

    private final BitmapFont font;
    private final Function<T, String> toText;
    private final Geometry selectionQuad;
    private final Node node = new Node();
    private final Map<T, BitmapText> keyToBitmapText = new LinkedHashMap<>();
    private T selected = null;

    public Listbox(BitmapFont font, Function<T, String> toText, Geometry selectionQuad) {
        this.font = font;
        this.toText = toText;
        this.selectionQuad = selectionQuad;
    }

    public void setList(List<T> keys) {
        if (new HashSet<>(keys).equals(keyToBitmapText.keySet())) {
            return;
        }
        for (BitmapText bitmapText : keyToBitmapText.values()) {
            node.remove(bitmapText);
        }
        keyToBitmapText.clear();
        int nextY = 0;
        for (T key : keys) {
            BitmapText bitmapText = new BitmapText(font);
            String text = toText.apply(key);
            if (text.isBlank()) {
                bitmapText.setText(" ");
            } else {
                bitmapText.setText(text);
            }
            bitmapText.setLocalTranslation(new Vector3f(0, nextY, 0));
            nextY += bitmapText.getTextHeight();
            keyToBitmapText.put(key, bitmapText);
            node.add(bitmapText);
        }
        if (selected != null && !keys.contains(selected)) {
            selected = null;
        }
        updateSelectionQuad();
    }

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        if (selected == null || keyToBitmapText.containsKey(selected)) {
            this.selected = selected;
        }
        updateSelectionQuad();
    }

    public void update() {
        int nextY = 0;
        for (Map.Entry<T, BitmapText> entry : keyToBitmapText.entrySet()) {
            BitmapText bitmapText = entry.getValue();
            String text = toText.apply(entry.getKey());
            if (text.isBlank()) {
                bitmapText.setText(" ");
            } else {
                bitmapText.setText(text);
            }
            bitmapText.setLocalTranslation(new Vector3f(0, nextY, 1));
            nextY += bitmapText.getTextHeight();
        }
        updateSelectionQuad();
    }

    public void onKey(KeyEvent event) {
        if (selected != null && event.getAction() == GLFW.GLFW_PRESS) {
            switch (event.getKey()) {
                case GLFW.GLFW_KEY_UP:
                    if (selected == null) {
                        if (!keyToBitmapText.isEmpty()) {
                            for (T key : keyToBitmapText.keySet()) {
                                selected = key;
                            }
                        }
                    } else {
                        List<T> keys = new ArrayList<>(keyToBitmapText.keySet());
                        for (int i = 1; i < keys.size(); i++) {
                            if (keys.get(i) == selected) {
                                selected = keys.get(i - 1);
                                break;
                            }
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_DOWN:
                    if (selected == null) {
                        if (!keyToBitmapText.isEmpty()) {
                            selected = keyToBitmapText.keySet().iterator().next();
                        }
                    } else {
                        List<T> keys = new ArrayList<>(keyToBitmapText.keySet());
                        for (int i = 0; i < keys.size() - 1; i++) {
                            if (keys.get(i) == selected) {
                                selected = keys.get(i + 1);
                                break;
                            }
                        }
                    }
                    break;
            }
            updateSelectionQuad();
        }
    }

    public void onMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            selected = null;
            if (0 <= cursorPosition.y) {
                int y = 0;
                for (Map.Entry<T, BitmapText> entry : keyToBitmapText.entrySet()) {
                    int height = entry.getValue().getTextHeight();
                    y += height;
                    if (cursorPosition.y < y) {
                        selected = entry.getKey();
                        break;
                    }
                }
            }
            updateSelectionQuad();
        }
    }

    private void updateSelectionQuad() {
        if (selected == null) {
            if (selectionQuad.hasParent(node)) {
                node.remove(selectionQuad);
            }
        } else {
            BitmapText bitmapText = keyToBitmapText.get(selected);
            selectionQuad.setLocalTranslation(new Vector3f(bitmapText.getLocalTransform().getTranslation()).setComponent(2, 0.999f));
            selectionQuad.setLocalScale(new Vector3f(bitmapText.getTextWidth(), bitmapText.getTextHeight(), 1));
            if (!selectionQuad.hasParent(node)) {
                node.add(selectionQuad);
            }
        }
    }

    public Node getNode() {
        return node;
    }
}
