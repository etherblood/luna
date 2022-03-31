package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.camera.systems.CameraKeyMoveSystem;
import com.destrostudios.icetea.core.camera.systems.CameraMouseRotateSystem;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.PlayerName;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.KHRSurface;

public class ApplicationClient extends Application {

    private final Set<Integer> pressedKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<Integer, ModelWrapper> models = new HashMap<>();
    private Geometry geometryGround;
    private final GameProxy gameProxy;

    private BitmapFont bitmapFont;
    private BitmapText screenStatsText;
    private long runningFrameSecond;
    private int runningFrameCount;
    private int frameCount;

    public ApplicationClient(GameProxy gameProxy) {
        super();
        setPreferredPresentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
        this.gameProxy = gameProxy;
    }

    @Override
    protected void initScene() {
        long nanos = System.nanoTime();
        GLFW.glfwSetWindowTitle(getWindow(), gameProxy.getPlayer().login);
        assetManager.addLocator(new FileLocator("./assets"));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.getAmbientColor().set(0.8f);
        directionalLight.setDirection(new Vector3f(2, -10, -5).normalize());
        directionalLight.addAffectedSpatial(sceneNode);
        directionalLight.addShadows(4096);
        setLight(directionalLight);

        sceneCamera.setLocation(new Vector3f(0, 2, 10));

        Shader vertexShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });
        Shader fragShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });

        // text

        bitmapFont = assetManager.loadBitmapFont("fonts/Verdana_18.fnt");

        screenStatsText = new BitmapText(bitmapFont, "Hello World.");
        screenStatsText.move(new Vector3f(0, 0, 1));
        guiNode.add(screenStatsText);


        // Ground

        Quad meshGround = new Quad(10, 10);

        Material materialGround = new Material();
        materialGround.setVertexShader(vertexShaderDefault);
        materialGround.setFragmentShader(fragShaderDefault);
        materialGround.getParameters().setVector4f("color", new Vector4f(0.2f, 0.2f, 0.2f, 1));

        geometryGround = new Geometry();
        geometryGround.setMesh(meshGround);
        geometryGround.setMaterial(materialGround);
        geometryGround.move(new Vector3f(-5, 0, 5));
        geometryGround.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryGround.setShadowMode(ShadowMode.RECEIVE);
        sceneNode.add(geometryGround);

        // Inputs

        CameraMouseRotateSystem cameraMouseRotateSystem = new CameraMouseRotateSystem(sceneCamera);
        CameraKeyMoveSystem cameraKeyMoveSystem = new CameraKeyMoveSystem(sceneCamera);
        inputManager.addKeyListener(keyEvent -> {
            if (keyEvent.getAction() == GLFW.GLFW_RELEASE) {
                pressedKeys.remove(keyEvent.getKey());
            } else {
                pressedKeys.add(keyEvent.getKey());
            }
            switch (keyEvent.getKey()) {
                case GLFW.GLFW_KEY_9:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        if (hasSystem(cameraMouseRotateSystem)) {
                            removeSystem(cameraMouseRotateSystem);
                            removeSystem(cameraKeyMoveSystem);
                        } else {
                            addSystem(cameraMouseRotateSystem);
                            addSystem(cameraKeyMoveSystem);
                        }
                    }
                    break;
            }
        });
        inputManager.addMouseButtonListener(mouseButtonEvent -> {
            if (mouseButtonEvent.getAction() == GLFW.GLFW_PRESS) {
                // placeholder
            }
        });
        System.out.println("init() in: " + (System.nanoTime() - nanos) / 1_000_000 + "ms");
    }

    @Override
    protected void update(float tpf) {
        GameEngine snapshot = gameProxy.getEngineSnapshot();
        EntityData data = snapshot.getData();

        for (ModelWrapper wrapper : models.values()) {
            if (!data.has(wrapper.getEntity(), ActorState.class)) {
                sceneNode.remove(wrapper.getNode());
                models.remove(wrapper.getEntity());
            }
        }

        for (int entity : data.list(ActorState.class)) {
            if (!models.containsKey(entity)) {
                long nanos = System.nanoTime();
                // hard coded amara model
                Node model = (Node) assetManager.loadModel("models/amara/amara.gltf");
                model.scale(new Vector3f(0.01f, 0.01f, 0.01f));
                model.setShadowMode(ShadowMode.CAST_AND_RECEIVE);

                String playerName = data.get(entity, PlayerName.class).name();

                BitmapText nameText = null;
//                BitmapText nameText = new BitmapText(bitmapFont, playerName);
////                nameText.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / 2), 1, 0, 0)));
//                nameText.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI), 0, 1, 0)));
//                nameText.move(new Vector3f(0, 5, 0));
//                model.add(nameText);

                models.put(entity, new ModelWrapper(entity, model, List.of("attack1", "attack2", "dash", "death", "idle", "walk"), nameText));
                System.out.println("load amara in: " + (System.nanoTime() - nanos) / 1_000_000 + "ms");
            }
            ModelWrapper wrapper = models.get(entity);
            Position position = data.get(entity, Position.class);
            if (position != null) {
                if (!wrapper.getNode().hasParent(sceneNode)) {
                    sceneNode.add(wrapper.getNode());
                }
                Vector2 vector = position.vector();
                wrapper.getNode().setLocalTranslation(convert(vector));
            } else {
                if (wrapper.getNode().hasParent(sceneNode)) {
                    sceneNode.remove(wrapper.getNode());
                }
            }

            ActorState actorState = data.get(entity, ActorState.class);
            if (actorState != null && actorState.direction() != Direction.NONE) {
                float angle1 = directionToAngle(actorState.direction());
                AxisAngle4f angle = new AxisAngle4f(angle1, 0, 1, 0);
                wrapper.getNode().setLocalRotation(new Quaternionf(angle));
            }

            if (actorState != null) {
                if (actorState.action() == ActorAction.IDLE) {
                    if (actorState.direction() == Direction.NONE) {
                        wrapper.setAnimation("idle");
                    } else {
                        wrapper.setAnimation("walk");
                    }
                } else if (actorState.action() == ActorAction.DASH) {
                    wrapper.setAnimation("dash");
                } else if (actorState.action() == ActorAction.ATTACK1) {
                    wrapper.setAnimation("attack1");
                } else if (actorState.action() == ActorAction.ATTACK2) {
                    wrapper.setAnimation("attack2");
                } else if (actorState.action() == ActorAction.DEATH) {
                    wrapper.setAnimation("death");
                }
            }
        }

        long player = gameProxy.getPlayer().id;
        gameProxy.requestInput(toInput(player, pressedKeys));
        gameProxy.update();

        long frameSecond = Math.floorDiv(System.nanoTime(), 1_000_000_000L);
        if (runningFrameSecond != frameSecond) {
            frameCount = runningFrameCount;
            runningFrameCount = 0;
            runningFrameSecond = frameSecond;
        }
        runningFrameCount++;
        screenStatsText.setText("fps: " + frameCount + "   latency: " + gameProxy.getLatency() + "ms");
    }

    private float directionToAngle(Direction direction) {
        Vector2 vector = direction.toLengthVector(1_000_000);
        return (float) Math.atan2(-vector.y(), vector.x()) + (float) (Math.PI / 2);
    }

    private Vector3f convert(Vector2 vector) {
        float milli = 0.001f;
        return new Vector3f(vector.x() * milli, 0, vector.y() * milli);
    }

    private static PlayerInput toInput(long player, Set<Integer> keyCodes) {
        int x = 0;
        int y = 0;
        if (keyCodes.contains(GLFW.GLFW_KEY_UP)) {
            y--;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_DOWN)) {
            y++;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_RIGHT)) {
            x++;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_LEFT)) {
            x--;
        }
        ActorAction action = ActorAction.IDLE;
        if (keyCodes.contains(GLFW.GLFW_KEY_X)) {
            action = ActorAction.DASH;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_1)) {
            action = ActorAction.ATTACK1;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_2)) {
            action = ActorAction.ATTACK2;
        }
        return new PlayerInput(player, Direction.of(x, y), action);
    }

}
