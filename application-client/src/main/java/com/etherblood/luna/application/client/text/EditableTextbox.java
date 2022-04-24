package com.etherblood.luna.application.client.text;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class EditableTextbox {
    // TODO: up/down navigation
    // TODO: text selection with double/triple click
    // TODO: text selection with shift click

    private final EditableText text = new EditableText(SelectionText.empty());
    private final Node node = new Node();
    private final Node quadNode = new Node();

    private final Consumer<String> commit;
    private final BitmapText bitmapText;
    private final Clipboard clipboard;
    private final Supplier<Geometry> quadSupply;
    private final List<Geometry> selectionQuads = new ArrayList<>();
    private boolean showSelection = false;
    private Vector2f selectionStart = null;

    public EditableTextbox(Consumer<String> commit, BitmapFont font, Material selectionMaterial, Clipboard clipboard) {
        this.commit = commit;
        bitmapText = new BitmapText(font);
        this.clipboard = clipboard;
        bitmapText.setLocalTranslation(new Vector3f(0, 0, 1));
        node.add(bitmapText);

        Mesh quad = new Quad(1, 1);
        quadSupply = () -> {
            Geometry g = new Geometry();
            g.setMesh(quad);
            g.setMaterial(selectionMaterial);
            g.setShadowMode(ShadowMode.OFF);
            return g;
        };
    }

    public void onKey(KeyEvent keyEvent) {
        if (keyEvent.getAction() == GLFW.GLFW_PRESS || keyEvent.getAction() == GLFW.GLFW_REPEAT) {
            boolean shift = (keyEvent.getModifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
            boolean ctrl = (keyEvent.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
            switch (keyEvent.getKey()) {
                case GLFW.GLFW_KEY_A:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        text.push(text.current().selectAll());
                    }
                    break;
                case GLFW.GLFW_KEY_V:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        String string = clipboard.get();
                        if (string != null) {
                            text.push(text.current().set(string));
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_C:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        String string = text.current().selected();
                        if (!string.isEmpty()) {
                            clipboard.set(string);
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_X:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS && ctrl) {
                        String string = text.current().selected();
                        if (!string.isEmpty()) {
                            clipboard.set(string);
                        }
                        text.push(text.current().set(""));
                    }
                    break;
                case GLFW.GLFW_KEY_Y:// german keyboard...
                case GLFW.GLFW_KEY_Z:
                    if (ctrl) {
                        if (shift) {
                            text.redo();
                        } else {
                            text.undo();
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_BACKSPACE:
                    text.push(text.current().deleteLeft(ctrl));
                    break;
                case GLFW.GLFW_KEY_DELETE:
                    text.push(text.current().deleteRight(ctrl));
                    break;
                case GLFW.GLFW_KEY_ENTER:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        if (shift) {
                            text.push(text.current().set("\n"));
                        } else {
                            String inputText = text.current().text();
                            commit.accept(inputText);
                            text.push(SelectionText.empty());
                        }
                    }
                    break;
                case GLFW.GLFW_KEY_LEFT:
                    text.push(text.current().left(shift, ctrl));
                    break;
                case GLFW.GLFW_KEY_RIGHT:
                    text.push(text.current().right(shift, ctrl));
                    break;
                case GLFW.GLFW_KEY_HOME:
                    text.push(text.current().fullLeft(shift));
                    break;
                case GLFW.GLFW_KEY_END:
                    text.push(text.current().fullRight(shift));
                    break;
            }
        }
    }

    public void onCharacter(CharacterEvent event) {
        text.push(text.current().set(Character.toString(event.getCodepoint())));
    }

    public void onMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                selectionStart = cursorPosition;
                updateSelection(cursorPosition);
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                updateSelection(cursorPosition);
                selectionStart = null;
            }
        }
    }

    public void onMouseMove(MousePositionEvent event) {
        updateSelection(new Vector2f((float) event.getX(), (float) event.getY()));
    }

    private void updateSelection(Vector2f cursorPosition) {
        if (selectionStart != null) {
            Vector3f offset = getNode().getLocalTransform().getTranslation();
            Vector2f selectionEnd = cursorPosition;
            setSelection(selectionStart.sub(offset.x(), offset.y(), new Vector2f()), selectionEnd.sub(offset.x(), offset.y(), new Vector2f()));
        }
    }

    public void update() {
        SelectionText current = text.current();
        String nextText = current.text();
        if (nextText.isBlank()) { // workaround for empty buffer crash
            nextText = " ";
        }
        bitmapText.setText(nextText);

        if (showSelection) {
            BitmapFont font = bitmapText.getFont();
            int min = Math.min(current.tail(), current.head());
            int max = Math.max(current.tail(), current.head());

            String[] lines = current.text().split("\n", -1);

            int minX = min;
            int minY = 0;
            while (minX > lines[minY].length()) {
                minX -= lines[minY].length() + 1;
                minY++;
            }

            int maxX = max;
            int maxY = 0;
            while (maxX > lines[maxY].length()) {
                maxX -= lines[maxY].length() + 1;
                maxY++;
            }

            int selectedLineCount = maxY - minY + 1;
            while (selectionQuads.size() > selectedLineCount) {
                Geometry quad = selectionQuads.remove(selectionQuads.size() - 1);
                quadNode.remove(quad);
            }
            while (selectionQuads.size() < selectedLineCount) {
                Geometry quad = quadSupply.get();
                selectionQuads.add(quad);
                quadNode.add(quad);
            }

            for (int i = 0; i < selectionQuads.size(); i++) {
                int y = i + minY;
                String line = lines[y];
                Geometry quad = selectionQuads.get(i);
                int startX = 0;
                if (y == minY) {
                    startX = minX;
                }
                int endX = line.length();
                if (y == maxY) {
                    endX = maxX;
                }
                String preText = line.substring(0, startX);
                String selectedText = line.substring(startX, endX);
                quad.setLocalTranslation(new Vector3f(font.getWidth(preText), y * font.getLineHeight(), 0.5f));
                quad.setLocalScale(new Vector3f(Math.max(1, font.getWidth(selectedText)), font.getLineHeight(), 1));
            }
        }
        if (showSelection != quadNode.hasParent(node)) {
            if (showSelection) {
                node.add(quadNode);
            } else {
                node.remove(quadNode);
            }
        }
    }

    public Node getNode() {
        return node;
    }

    public EditableText getText() {
        return text;
    }

    private void setSelection(Vector2f mouseTail, Vector2f mouseHead) {
        BitmapFont font = bitmapText.getFont();
        SelectionText current = text.current();
        String currentText = current.text();

        String[] lines = currentText.split("\n", -1);
        int tailY = Math.max(0, Math.min(lines.length - 1, (int) Math.floor(mouseTail.y / font.getLineHeight())));
        int headY = Math.max(0, Math.min(lines.length - 1, (int) Math.floor(mouseHead.y / font.getLineHeight())));

        int tailX = toCursorIndexX(font, lines[tailY], mouseTail.x());
        int headX = toCursorIndexX(font, lines[headY], mouseHead.x());

        int tail = tailX;
        for (int i = 0; i < tailY; i++) {
            tail += lines[i].length() + 1;
        }

        int head = headX;
        for (int i = 0; i < headY; i++) {
            head += lines[i].length() + 1;
        }

        text.push(current.select(tail, head));
    }

    private static int toCursorIndexX(BitmapFont font, String text, float x) {
        int i = 0;
        while (i < text.length() && Math.abs(x - font.getWidth(text.substring(0, i))) > Math.abs(x - font.getWidth(text.substring(0, i + 1)))) {
            i++;
        }
        return i;
    }

    public void setShowSelection(boolean showSelection) {
        this.showSelection = showSelection;
    }
}
