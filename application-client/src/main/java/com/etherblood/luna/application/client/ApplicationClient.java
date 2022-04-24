package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
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
        config.setPreferredPresentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
        this.gameProxy = gameProxy;
    }

    @Override
    protected void init() {
        try (PrintStopwatch stopwatch = new PrintStopwatch("init")) {
            super.init();
            GLFW.glfwSetWindowTitle(getWindow(), gameProxy.getPlayer().login);
            assetManager.addLocator(new FileLocator("./assets"));

            // text
            bitmapFont = assetManager.loadBitmapFont("fonts/Verdana_18.fnt");

            screenStatsText = new BitmapText(bitmapFont, "Connecting...");
            screenStatsText.move(new Vector3f(0, 0, 1));
            guiNode.add(screenStatsText);

            guiNode.setRenderBucket(RenderBucketType.GUI);

            addSystem(new GameSystem(gameProxy));
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
