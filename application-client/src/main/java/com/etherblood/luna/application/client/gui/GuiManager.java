package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class GuiManager extends LifecycleObject implements InputLayer {
    private final GuiFactory factory;
    private GuiContainer rootContainer;

    public GuiManager(GuiFactory factory) {
        this.factory = factory;
    }

    @Override
    protected void init() {
        super.init();
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(application.getWindow(), width, height);
        rootContainer = factory.container(new BoundingRectangle(0, 0, width[0], height[0]));
        rootContainer.onFocus(true);
        rootContainer.node().setLocalScale(new Vector3f(1, 1, 0.5f));
        rootContainer.node().setLocalTranslation(new Vector3f(0, 0, 0.25f));
        application.getGuiNode().add(rootContainer.node());
    }

    @Override
    protected void cleanupInternal() {
        super.cleanupInternal();
        application.getGuiNode().remove(rootContainer.node());
        rootContainer.cleanup();
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(application.getWindow(), width, height);
        rootContainer.setBounds(new BoundingRectangle(0, 0, width[0], height[0]));
    }

    public GuiFactory getFactory() {
        return factory;
    }

    public GuiContainer getRootContainer() {
        return rootContainer;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.GUI;
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        return rootContainer.consumeKey(event);
    }

    @Override
    public boolean consumeCharacter(CharacterEvent event, String character) {
        return rootContainer.consumeCharacter(event, character);
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        return rootContainer.consumeMouseButton(event, cursorPosition);
    }

    @Override
    public boolean consumeMouseMove(MousePositionEvent event) {
        return rootContainer.consumeMouseMove(event, new Vector2f((float) event.getX(), (float) event.getY()));
    }
}
