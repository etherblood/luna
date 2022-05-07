package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class GuiContainer extends BaseGuiElement {

    private final List<GuiElement> childs = new ArrayList<>();
    private GuiElement focusedChild = null;

    public GuiContainer(Spatial background, BoundingRectangle bounds) {
        super(background, bounds);
    }

    public void add(GuiElement child) {
        childs.add(0, child);
        node().add(child.node());
        updateChildsZ();
    }

    public void remove(GuiElement child) {
        if (child == focusedChild) {
            setFocusedChild(null);
        }
        childs.remove(child);
        node().remove(child.node());
        updateChildsZ();
    }

    private void updateChildsZ() {
        List<GuiElement> children = getChilds();
        for (int i = 0; i < children.size(); i++) {
            GuiElement other = children.get(i);
            Vector3f translation = other.node().getLocalTransform().getTranslation();
            float z = BaseGuiElement.MAX_Z - (i + 1) * BaseGuiElement.MAX_Z / children.size();
            other.node().setLocalTranslation(new Vector3f(translation).setComponent(2, z));
            other.node().setLocalScale(new Vector3f(1, 1, BaseGuiElement.MAX_Z / children.size()));
        }
    }

    public List<GuiElement> getChilds() {
        return childs;
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        // TODO: add keyboard focus navigation
        return (focusedChild != null && focusedChild.consumeKey(event)) || super.consumeKey(event);
    }

    @Override
    public boolean consumeCharacter(CharacterEvent event, String character) {
        return (focusedChild != null && focusedChild.consumeCharacter(event, character)) || super.consumeCharacter(event, character);
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        Vector2f childCursor = cursorPosition.sub(bounds.x(), bounds.y(), new Vector2f());
        if (event.getAction() == GLFW.GLFW_PRESS && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            GuiElement nextChild = null;
            for (GuiElement child : childs) {
                if (child.bounds().contains(childCursor)) {
                    nextChild = child;
                    break;
                }
            }
            setFocusedChild(nextChild);
        }
        return focusedChild != null && focusedChild.consumeMouseButton(event, childCursor) || super.consumeMouseButton(event, cursorPosition);
    }

    @Override
    public boolean consumeMouseMove(MousePositionEvent event, Vector2f cursorPosition) {
        Vector2f childCursor = cursorPosition.sub(bounds.x(), bounds.y(), new Vector2f());
        for (GuiElement child : getChilds()) {
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
        if (child != null && !getChilds().contains(child)) {
            throw new IllegalArgumentException("Can't focus element that is not child of this container.");
        }
        if (focusedChild == child) {
            return;
        }
        if (focusedChild != null) {
            focusedChild.onFocus(false);
        }
        focusedChild = child;
        if (focusedChild != null) {
            focusedChild.onFocus(true);
        }
    }

    @Override
    public void onFocus(boolean focus) {
        super.onFocus(focus);
        if (!focus) {
            setFocusedChild(null);
        }
    }
}
