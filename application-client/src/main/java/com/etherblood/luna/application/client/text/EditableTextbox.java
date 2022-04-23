package com.etherblood.luna.application.client.text;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class EditableTextbox {

    private final EditableText text = new EditableText(SelectionText.empty());
    private final Node node = new Node();
    private final Node quadNode = new Node();

    private final BitmapText bitmapText;

    private final Supplier<Geometry> quadSupply;
    private final List<Geometry> selectionQuads = new ArrayList<>();
    private boolean showSelection = false;

    public EditableTextbox(BitmapFont font, Material selectionMaterial) {
        bitmapText = new BitmapText(font);
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

    public void setSelection(Vector2f mouseTail, Vector2f mouseHead) {
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
