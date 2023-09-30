package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class BaseGuiElement implements GuiElement {

    public static final float MAX_Z = 0.99f;
    protected final Node node = new Node();
    protected final Spatial background;
    protected boolean focused = false;
    protected BoundingRectangle bounds;

    public BaseGuiElement(Spatial background, BoundingRectangle bounds) {
        this.background = background;
        if (background != null) {
            background.setLocalTranslation(new Vector3f(0, 0, 0));
            node().add(background);
        }
        updateNodesBounds(bounds);
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        if (!focused) {
            throw new IllegalStateException("Only focused elements must receive key events.");
        }
        return false;
    }

    @Override
    public boolean consumeCharacter(CharacterEvent event, String character) {
        if (!focused) {
            throw new IllegalStateException("Only focused elements must receive character events.");
        }
        return false;
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        return false;
    }

    @Override
    public boolean consumeMouseMove(MousePositionEvent event, Vector2f cursorPosition) {
        return false;
    }

    @Override
    public void onFocus(boolean focus) {
        focused = focus;
    }

    public void cleanup() {
        node.cleanupNativeState();
        if (background != null) {
            background.cleanupNativeState();
        }
    }

    public Spatial getBackground() {
        return background;
    }

    protected void setBounds(BoundingRectangle newBounds) {
        updateNodesBounds(newBounds);
    }

    private void updateNodesBounds(BoundingRectangle newBounds) {
        if (bounds != null && bounds.equals(newBounds)) {
            return;
        }
        bounds = newBounds;
        Vector3f translation = new Vector3f(bounds.x(), bounds.y(), 0);
        node.setLocalTranslation(translation);
        if (background != null) {
            Vector3f scale = new Vector3f(bounds.width(), bounds.height(), 1);
            background.setLocalScale(scale);
        }
    }

    @Override
    public BoundingRectangle bounds() {
        return bounds;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }
}
