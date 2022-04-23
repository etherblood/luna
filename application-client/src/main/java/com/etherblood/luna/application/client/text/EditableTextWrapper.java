package com.etherblood.luna.application.client.text;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class EditableTextWrapper {

    // TODO: add multi line support

    private final EditableText text = new EditableText(SelectionText.empty());
    private final Node node = new Node();

    private final BitmapText bitmapText;

    private final Geometry selectionQuad;// TODO: multiple selection quads for multiple lines

    public EditableTextWrapper(BitmapFont font, Material selectionMaterial) {
        bitmapText = new BitmapText(font);
        bitmapText.setLocalTranslation(new Vector3f(0, 0, 1));
        node.add(bitmapText);

        Mesh quad = new Quad(1, 1);
        selectionQuad = new Geometry();
        selectionQuad.setMesh(quad);
        selectionQuad.setMaterial(selectionMaterial);
        selectionQuad.setShadowMode(ShadowMode.OFF);

        node.add(selectionQuad);
    }

    public void update() {
        SelectionText current = text.current();
        String nextText = current.text();
        if (nextText.isEmpty()) { // workaround for empty buffer crash
            nextText = " ";
        }
        if (!bitmapText.getText().equals(nextText)) { // workaround for expensive text buffer update
            bitmapText.setText(nextText);
        }

        BitmapFont font = bitmapText.getFont();
        int min = Math.min(current.tail(), current.head());
        int max = Math.max(current.tail(), current.head());
        int x = font.getWidth(nextText.substring(0, min));
        int width = font.getWidth(nextText.substring(min, max));
        selectionQuad.setLocalTranslation(new Vector3f(x, 0, 0.5f));
        selectionQuad.setLocalScale(new Vector3f(Math.max(1, width), font.getLineHeight(), 1));

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

        int tail = toCursorIndex(font, currentText, mouseTail.x());
        int head = toCursorIndex(font, currentText, mouseHead.x());
        text.push(current.select(tail, head));
    }

    private static int toCursorIndex(BitmapFont font, String text, float x) {
        int i = 0;
        while (i < text.length() && Math.abs(x - font.getWidth(text.substring(0, i))) > Math.abs(x - font.getWidth(text.substring(0, i + 1)))) {
            i++;
        }
        return i;
    }
}
