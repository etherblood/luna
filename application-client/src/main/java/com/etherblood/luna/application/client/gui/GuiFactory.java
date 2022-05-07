package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.application.client.gui.textbox.Clipboard;
import com.etherblood.luna.application.client.gui.textbox.EditableTextbox;
import com.etherblood.luna.application.client.gui.textbox.WindowClipboard;
import java.util.function.Consumer;
import java.util.function.Function;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VK10;

public class GuiFactory extends LifecycleObject {

    private final Quad unitQuad = new Quad(1, 1);
    private Clipboard clipboard;
    private Material selectionMaterial;
    private Material backgroundMaterial;
    private BitmapFont font;

    @Override
    protected void init() {
        super.init();

        clipboard = new WindowClipboard(application.getWindow());
        Shader vertexShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });
        Shader fragShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });

        selectionMaterial = new Material();
        selectionMaterial.setVertexShader(vertexShaderDefault);
        selectionMaterial.setFragmentShader(fragShaderDefault);
        selectionMaterial.setCullMode(VK10.VK_CULL_MODE_NONE);
        selectionMaterial.getParameters().setVector4f("color", new Vector4f(11 / 255f, 104 / 255f, 217 / 255f, 1));
        selectionMaterial.setDepthTest(false);
        selectionMaterial.setDepthWrite(false);

        font = application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt");

        backgroundMaterial = new Material(selectionMaterial, CloneContext.cloneOnlyMaterials());
        backgroundMaterial.getParameters().setVector4f("color", new Vector4f(0.5f, 0.5f, 0.5f, 0.75f));
    }

    @Override
    protected void cleanupInternal() {
        super.cleanupInternal();

        selectionMaterial.cleanup();
        backgroundMaterial.cleanup();
    }

    public GuiContainer container(BoundingRectangle bounds) {
        return new GuiContainer(null, bounds);
    }

    public GuiContainer filledContainer(BoundingRectangle bounds) {
        return new GuiContainer(backgroundQuad(), bounds);
    }

    public EditableTextbox editableTextbox(BoundingRectangle bounds, Consumer<String> commit) {
        return new EditableTextbox(backgroundQuad(), bounds, font, this::selectionQuad, clipboard, commit);
    }

    public <T> Listbox<T> listbox(BoundingRectangle bounds, Function<T, String> toText) {
        return new Listbox<>(backgroundQuad(), bounds, font, toText, selectionQuad());
    }

    public Button button(BoundingRectangle bounds, Runnable confirm) {
        return new Button(backgroundQuad(), bounds, bitmapText(), confirm);
    }

    public Geometry selectionQuad() {
        Geometry geometry = new Geometry();
        geometry.setMesh(unitQuad);
        geometry.setMaterial(selectionMaterial);
        return geometry;
    }

    public Geometry backgroundQuad() {
        Geometry geometry = new Geometry();
        geometry.setMesh(unitQuad);
        geometry.setMaterial(backgroundMaterial);
        return geometry;
    }

    public BitmapFont bitmapFont() {
        return font;
    }

    public BitmapText bitmapText() {
        return new BitmapText(font, "UNINITIALIZED");
    }
}
