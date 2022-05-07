package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Spatial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Listbox<T> extends BaseGuiElement {

    private final BitmapFont font;
    private final Function<T, String> toText;
    private final Geometry selectionQuad;
    private final Map<T, BitmapText> keyToBitmapText = new LinkedHashMap<>();
    private T selected = null;

    public Listbox(Spatial background, BoundingRectangle bounds, BitmapFont font, Function<T, String> toText, Geometry selectionQuad) {
        super(background, bounds);
        this.font = font;
        this.toText = toText;
        this.selectionQuad = selectionQuad;
    }

    public void setList(List<T> keys) {
        if (new HashSet<>(keys).equals(keyToBitmapText.keySet())) {
            return;
        }
        for (BitmapText bitmapText : keyToBitmapText.values()) {
            node().remove(bitmapText);
        }
        keyToBitmapText.clear();
        int nextY = 0;
        for (T key : keys) {
            String text = toText.apply(key);
            if (text.isBlank()) {
                text = " ";
            }
            BitmapText bitmapText = new BitmapText(font, text);
            bitmapText.setLocalTranslation(new Vector3f(0, nextY, BaseGuiElement.MAX_Z));
            nextY += bitmapText.getTextHeight();
            keyToBitmapText.put(key, bitmapText);
            node().add(bitmapText);
        }
        if (selected != null && !keys.contains(selected)) {
            selected = null;
        }
        updateListText();
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

    private void updateListText() {
        int nextY = 0;
        for (Map.Entry<T, BitmapText> entry : keyToBitmapText.entrySet()) {
            BitmapText bitmapText = entry.getValue();
            String text = toText.apply(entry.getKey());
            if (text.isBlank()) {
                bitmapText.setText(" ");
            } else {
                bitmapText.setText(text);
            }
            bitmapText.setLocalTranslation(new Vector3f(0, nextY, BaseGuiElement.MAX_Z));
            nextY += bitmapText.getTextHeight();
        }
        updateSelectionQuad();
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        boolean consume = false;
        if (event.getAction() == GLFW.GLFW_PRESS) {
            switch (event.getKey()) {
                case GLFW.GLFW_KEY_UP:
                    consume = true;
                    if (selected == null) {
                        for (T key : keyToBitmapText.keySet()) {
                            selected = key;
                        }
                    } else {
                        List<T> keys = new ArrayList<>(keyToBitmapText.keySet());
                        for (int i = 1; i < keys.size(); i++) {
                            if (keys.get(i).equals(selected)) {
                                selected = keys.get(i - 1);
                                break;
                            }
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_DOWN:
                    consume = true;
                    if (selected == null) {
                        Iterator<T> iterator = keyToBitmapText.keySet().iterator();
                        if (iterator.hasNext()) {
                            selected = iterator.next();
                        }
                    } else {
                        List<T> keys = new ArrayList<>(keyToBitmapText.keySet());
                        for (int i = 0; i < keys.size() - 1; i++) {
                            if (keys.get(i).equals(selected)) {
                                selected = keys.get(i + 1);
                                break;
                            }
                        }
                    }
                    break;
            }
            updateSelectionQuad();
        }
        return consume;
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        Vector2f localCursor = cursorPosition.sub(bounds.x(), bounds.y(), new Vector2f());
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            selected = null;
            if (0 <= localCursor.y) {
                int y = 0;
                for (Map.Entry<T, BitmapText> entry : keyToBitmapText.entrySet()) {
                    int height = entry.getValue().getTextHeight();
                    y += height;
                    if (localCursor.y < y) {
                        selected = entry.getKey();
                        break;
                    }
                }
            }
            updateSelectionQuad();
            return true;
        }
        return false;
    }

    private void updateSelectionQuad() {
        if (selected == null) {
            if (selectionQuad.hasParent(node())) {
                node().remove(selectionQuad);
            }
        } else {
            BitmapText bitmapText = keyToBitmapText.get(selected);
            selectionQuad.setLocalTranslation(new Vector3f(bitmapText.getLocalTransform().getTranslation()).setComponent(2, 0.5f));
            selectionQuad.setLocalScale(new Vector3f(bitmapText.getTextWidth(), bitmapText.getTextHeight(), 1));
            if (!selectionQuad.hasParent(node())) {
                node().add(selectionQuad);
            }
        }
    }
}
