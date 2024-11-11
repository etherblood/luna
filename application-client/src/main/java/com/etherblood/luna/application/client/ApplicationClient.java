package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.etherblood.luna.application.client.game.GameProxy;
import com.etherblood.luna.application.client.game.GameSystem;
import com.etherblood.luna.application.client.gui.GuiFactory;
import com.etherblood.luna.application.client.gui.GuiManager;
import com.etherblood.luna.application.client.gui.InputLayersSystem;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.KHRSurface;

public class ApplicationClient extends Application {

    private final GameProxy gameProxy;

    private BitmapFont bitmapFont;
    private BitmapText screenStatsText;
    private long runningFrameSecond;
    private int runningFrameCount;
    private int frameCount;

    public ApplicationClient(GameProxy gameProxy) {
        config.setClearColor(new Vector4f(0.2f, 0.15f, 0.15f, 1));
        config.setPreferredPresentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);// vsync
        this.gameProxy = gameProxy;
    }

    public <T> T getSystem(Class<T> type) {
        return getSystems().stream().filter(type::isInstance).map(type::cast).findAny().orElse(null);
    }

    @Override
    protected void init() {
        try (PrintStopwatch stopwatch = new PrintStopwatch(getClass().getSimpleName() + ".init()")) {
            super.init();
            GLFW.glfwSetWindowTitle(getWindow(), gameProxy.getPlayer().login);
            assetManager.addLocator(new FileLocator("./assets"));

            // text
            bitmapFont = assetManager.loadBitmapFont("fonts/Verdana_18.fnt");

            screenStatsText = new BitmapText(bitmapFont, "Connecting...");
            screenStatsText.move(new Vector3f(0, 0, 1));
            guiNode.add(screenStatsText);

            guiNode.setRenderBucket(RenderBucketType.GUI);

            GuiFactory guiFactory = new GuiFactory();
            addSystem(guiFactory);
            addSystem(new GuiManager(guiFactory));
            addSystem(new GameSystem(gameProxy, guiFactory));
            addSystem(new InputLayersSystem());
        }
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        updateStats();
    }

    private void updateStats() {
        long frameSecond = Math.floorDiv(System.nanoTime(), 1_000_000_000L);
        if (runningFrameSecond != frameSecond) {
            frameCount = runningFrameCount;
            runningFrameCount = 0;
            runningFrameSecond = frameSecond;
        }
        runningFrameCount++;
        screenStatsText.setText("fps: " + frameCount + "\nping: " + gameProxy.getLatency() + "ms");
    }

}
