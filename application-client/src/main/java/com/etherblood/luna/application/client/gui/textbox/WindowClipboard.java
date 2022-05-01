package com.etherblood.luna.application.client.gui.textbox;

import org.lwjgl.glfw.GLFW;

public class WindowClipboard implements Clipboard {
    private final long window;

    public WindowClipboard(long window) {
        this.window = window;
    }

    @Override
    public void set(String value) {
        GLFW.glfwSetClipboardString(window, value);
    }

    @Override
    public String get() {
        return GLFW.glfwGetClipboardString(window);
    }
}
