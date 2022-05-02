package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class GuiContainer extends BaseGuiElement {

    private final List<GuiElement> childs = new ArrayList<>();
    private GuiElement focusedChild = null;

    public void add(GuiElement child) {
        add(childs.size(), child);
    }

    public void add(int index, GuiElement child) {
        childs.add(index, child);
        node.add(child.node());
        updateChildTransforms();
    }

    public void remove(GuiElement child) {
        if (child == focusedChild) {
            setFocusedChild(null);
        }
        childs.remove(child);
        if (child.node().hasParent(node)) {
            node.remove(child.node());
        }
        updateChildTransforms();
    }

    private void updateChildTransforms() {
        for (int i = 0; i < childs.size(); i++) {
            GuiElement other = childs.get(i);
            Vector3f translation = other.node().getLocalTransform().getTranslation();
            other.node().setLocalTranslation(new Vector3f(translation).setComponent(2, (float) i / childs.size()));
            other.node().setLocalScale(new Vector3f(1, 1, 1f / childs.size()));
        }
    }

    public List<GuiElement> getChilds() {
        return Collections.unmodifiableList(childs);
    }

    @Override
    public void update() {
        for (GuiElement child : childs) {
            child.update();
        }
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        // TODO: add keyboard focus navigation
        return (focusedChild != null && focusedChild.consumeKey(event)) || super.consumeKey(event);
    }

    @Override
    public boolean consumeCharacter(CharacterEvent event) {
        return (focusedChild != null && focusedChild.consumeCharacter(event)) || super.consumeCharacter(event);
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        for (GuiElement child : childs) {
            Vector3f childTranslation = child.node().getLocalTransform().getTranslation();
            Vector2f childCursor = new Vector2f(cursorPosition.x - childTranslation.x, cursorPosition.y - childTranslation.y);
            if (child.consumeMouseButton(event, childCursor)) {
                setFocusedChild(child);
                return true;
            }
        }
        setFocusedChild(null);
        return super.consumeMouseButton(event, cursorPosition);
    }

    @Override
    public boolean consumeMouseMove(MousePositionEvent event, Vector2f cursorPosition) {
        for (GuiElement child : childs) {
            Vector3f childTranslation = child.node().getLocalTransform().getTranslation();
            Vector2f childCursor = new Vector2f(cursorPosition.x - childTranslation.x, cursorPosition.y - childTranslation.y);
            if (child.consumeMouseMove(event, childCursor)) {
                return true;
            }
        }
        return super.consumeMouseMove(event, cursorPosition);
    }

    public GuiElement getFocusedChild() {
        return focusedChild;
    }

    public void setFocusedChild(GuiElement child) {
        if (!focused
                || !childs.contains(child)
                || focusedChild == child) {
            return;
        }
        if (focusedChild != null) {
            focusedChild.onFocus(false);
        }
        if (child != null) {
            child.onFocus(true);
        }
        focusedChild = child;
    }

    @Override
    public void onFocus(boolean focus) {
        super.onFocus(focus);
        if (!focus) {
            setFocusedChild(null);
        }
    }
}
