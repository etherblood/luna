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
    protected final Node node = new Node();
    protected Spatial background;
    protected final Vector2f size = new Vector2f(100, 100);
    protected boolean focused = false;

    @Override
    public Node node() {
        return node;
    }

    @Override
    public void update() {
//        node.setLocalTranslation(new Vector3f(bounds.minX, bounds.minY, 1));
        background.setLocalScale(new Vector3f(size.x, size.y, 1));
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        return false;
    }

    @Override
    public boolean consumeCharacter(CharacterEvent event) {
        return false;
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        return background != null && contains(cursorPosition);
    }

    @Override
    public boolean consumeMouseMove(MousePositionEvent event, Vector2f cursorPosition) {
        return background != null && contains(cursorPosition);
    }

    @Override
    public void onFocus(boolean focus) {
        focused = focus;
    }

    protected boolean contains(Vector2f v) {
        return 0 <= v.x && v.x < size.x
                && 0 <= v.y && v.y < size.y;
    }
}
