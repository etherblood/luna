package com.etherblood.luna.application.client.game;

import com.destrostudios.icetea.core.AppSystem;
import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowConfig;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.application.client.PrintStopwatch;
import com.etherblood.luna.application.client.gui.GuiFactory;
import com.etherblood.luna.application.client.gui.InputLayer;
import com.etherblood.luna.application.client.gui.LayerOrder;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActiveAction;
import com.etherblood.luna.engine.ActorName;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.ModelKey;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.data.ActionAnimation;
import com.etherblood.luna.engine.actions.data.ActionDuration;
import com.etherblood.luna.engine.actions.data.ActionType;
import com.etherblood.luna.engine.damage.MilliHealth;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameSystem extends AppSystem implements InputLayer {

    private final float CAMERA_DISTANCE = 10;
    private final float CAMERA_ANGLE = (float) (30 * Math.PI / 180);

    private final Set<Integer> pressedKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());
    //    private final Set<Integer> pressedKeyDeltas = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<Integer, SceneModel> models = new HashMap<>();
    private final Map<Integer, SceneStatusHud> statusHuds = new HashMap<>();
    private Geometry geometryGround;
    private final GameProxy gameProxy;
    private final GuiFactory guiFactory;
    private UUID loadedGame = null;

    public GameSystem(GameProxy gameProxy, GuiFactory guiFactory) {
        this.gameProxy = gameProxy;
        this.guiFactory = guiFactory;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.GAME;
    }

    @Override
    public void onAttached() {
        try (PrintStopwatch stopwatch = new PrintStopwatch("init")) {
            super.onAttached();

            GLFW.glfwSetWindowTitle(application.getWindow(), gameProxy.getPlayer().login);
            application.getAssetManager().addLocator(new FileLocator("./assets"));

            DirectionalLight directionalLight = new DirectionalLight();
            directionalLight.getAmbientColor().set(0.75f, 0.75f, 0.75f, 1);
            directionalLight.getLightColor().set(0.75f, 0.75f, 0.75f, 1);
            directionalLight.setDirection(new Vector3f(0, -1, 0).normalize());
            directionalLight.addAffectedSpatial(application.getSceneNode());
            directionalLight.addShadows(new ShadowConfig());
            application.setLight(directionalLight);

            application.getSceneCamera().setLocation(new Vector3f(0, 2, 10));
            application.getSceneCamera().setRotation(new Quaternionf(new AxisAngle4f(CAMERA_ANGLE, 1, 0, 0)));


            // Ground
            Shader vertexShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[]{
                    "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                    "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
            });
            Shader fragShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[]{
                    "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                    "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
            });

            Material groundMaterial = new Material();
            groundMaterial.setVertexShader(vertexShaderDefault);
            groundMaterial.setFragmentShader(fragShaderDefault);
            groundMaterial.getParameters().setVector4f("color", new Vector4f(0.2f, 0.2f, 0.2f, 1));

            geometryGround = new Geometry();// hacky hack (:
            geometryGround.setMesh(new Quad(1, 1));
            geometryGround.setMaterial(groundMaterial);
            geometryGround.setLocalScale(new Vector3f(10, 10, 1));
            geometryGround.move(new Vector3f(-5, 0, 5));
            geometryGround.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
            geometryGround.setShadowMode(ShadowMode.RECEIVE);
            application.getSceneNode().add(geometryGround);
        }
    }

    @Override
    public boolean consumeKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_RELEASE) {
            pressedKeys.remove(event.getKey());
        } else {
            pressedKeys.add(event.getKey());
        }
//        if (event.getAction() == GLFW.GLFW_RELEASE || event.getAction() == GLFW.GLFW_PRESS) {
//            if (pressedKeyDeltas.contains(event.getKey())) {
//                pressedKeyDeltas.remove(event.getKey());
//            } else {
//                pressedKeyDeltas.add(event.getKey());
//            }
//        }
        return true;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
//        gameProxy.update(toInput(gameProxy.getPlayer().id, pressedKeyDeltas));
//        pressedKeyDeltas.clear();
        gameProxy.update(toInput(gameProxy.getPlayer().id, pressedKeys));
        GameEngine snapshot = gameProxy.getEngineSnapshot();
        if (snapshot == null) {
            return;
        }
        EntityData data = snapshot.getData();

        preloadIfRequired(snapshot);
        updateCamera(data);
        updateActorHuds(data);
        updateModels(snapshot);
    }

    private void updateCamera(EntityData data) {
        List<Integer> players = data.findByValue(new PlayerId(gameProxy.getPlayer().id));
        for (int player : players) {
            Position position = data.get(player, Position.class);
            if (position != null) {
                Vector3f lookAt = convert(position.vector());
                Vector3f cameraOffset = new Vector3f(0, 0, CAMERA_DISTANCE);
                cameraOffset.rotate(new Quaternionf(new AxisAngle4f(-CAMERA_ANGLE, 1, 0, 0)));
                Vector3f lookFrom = lookAt.add(cameraOffset, new Vector3f());
                application.getSceneCamera().setLocation(lookFrom);
            }
        }
    }

    private void updateActorHuds(EntityData data) {
        Node guiNode = application.getGuiNode();
        Iterator<SceneStatusHud> iterator = statusHuds.values().iterator();
        while (iterator.hasNext()) {
            SceneStatusHud wrapper = iterator.next();
            if (!data.has(wrapper.getEntity(), ActorName.class)
                    && !data.has(wrapper.getEntity(), MilliHealth.class)) {
                guiNode.remove(wrapper.getNode());
                iterator.remove();
            }
        }
        List<Integer> entities = new ArrayList<>();
        entities.addAll(data.list(ActorName.class));
        entities.addAll(data.list(MilliHealth.class));
        for (int entity : entities) {
            if (!statusHuds.containsKey(entity)) {
                SceneStatusHud wrapper = new SceneStatusHud(entity, guiFactory.bitmapFont());
                statusHuds.put(entity, wrapper);
                guiNode.add(wrapper.getNode());
            }
            Vector2 position = data.get(entity, Position.class).vector();
            ActorName actorName = data.get(entity, ActorName.class);
            String name = actorName == null ? null : actorName.name();
            MilliHealth health = data.get(entity, MilliHealth.class);
            SceneStatusHud wrapper = statusHuds.get(entity);
            wrapper.setName(name);
            wrapper.setHealth(health == null ? null : health.value());
            Vector3f screenCoordinates = application.getScreenCoordinates(convert(position));
            wrapper.getNode().setLocalTranslation(screenCoordinates.setComponent(2, 0));
        }
    }

    private void updateModels(GameEngine snapshot) {
        Node sceneNode = application.getSceneNode();
        EntityData data = snapshot.getData();
        Iterator<SceneModel> iterator = models.values().iterator();
        while (iterator.hasNext()) {
            SceneModel model = iterator.next();
            if (!data.has(model.entity(), ModelKey.class)) {
                if (model.node().hasParent(sceneNode)) {
                    sceneNode.remove(model.node());
                }
                iterator.remove();
            }
        }

        for (int entity : data.list(ModelKey.class)) {
            String name = data.get(entity, ModelKey.class).name();
            if (!models.containsKey(entity)) {
                Geometry model = loadModel(name);
                models.put(entity, new SceneModel(entity, model));
            }
            SceneModel model = models.get(entity);
            Position position = data.get(entity, Position.class);
            if (position != null) {
                if (!model.node().hasParent(sceneNode)) {
                    sceneNode.add(model.node());
                }
                Vector2 vector = position.vector();
                model.node().setLocalTranslation(convert(vector));
            } else {
                if (model.node().hasParent(sceneNode)) {
                    sceneNode.remove(model.node());
                }
            }

            Direction direction = data.get(entity, Direction.class);
            if (direction != null) {
                float angle = directionToAngle(direction);
                AxisAngle4f axisAngle = new AxisAngle4f(angle, 0, 1, 0);
                model.node().setLocalRotation(new Quaternionf(axisAngle));
            }

            ActiveAction activeAction = data.get(entity, ActiveAction.class);
            if (activeAction != null) {
                String animation = data.get(activeAction.action(), ActionAnimation.class).animationName();
                float fps = snapshot.getRules().getFramesPerSecond();
                // - 1 because tick count increases after game update & before render
                long animationFrames = snapshot.getFrame() - activeAction.startFrame() - 1;

                ActionDuration duration = data.get(activeAction.action(), ActionDuration.class);
                if (duration != null) {
                    long targetFrames = duration.frames();
                    float progress = (float) animationFrames / targetFrames;
                    model.setAnimationProgress(animation, progress);
                } else {
                    model.setAnimationTime(animation, animationFrames / fps);
                }
            }
        }
    }

    private void preloadIfRequired(GameEngine snapshot) {
        if (!snapshot.getId().equals(loadedGame)) {
            try (PrintStopwatch stopwatch = new PrintStopwatch("preload total")) {
                // very hacky...
                GameEngine preloadGame = snapshot.getRules().createGame();
                for (String templateKey : new HashSet<>(preloadGame.getTemplates().templateKeys())) {
                    int entity = preloadGame.getData().createEntity();
                    preloadGame.applyTemplate(entity, templateKey);
                }
                Node preloadNode = new Node();
                for (String name : preloadGame.getData().list(ModelKey.class).stream()
                        .map(entity -> preloadGame.getData().get(entity, ModelKey.class).name())
                        .collect(Collectors.toSet())) {
                    try (PrintStopwatch sub = new PrintStopwatch("preload " + name)) {
                        Geometry geometry = loadModel(name);
                        preloadNode.add(geometry);
                    }
                }
                try (PrintStopwatch sub = new PrintStopwatch("update internal state")) {
                    Node sceneNode = application.getSceneNode();
                    sceneNode.add(preloadNode);
                    application.updateInternalState();
                    sceneNode.remove(preloadNode);
                }
                loadedGame = snapshot.getId();
            }
        }
    }

    private Geometry loadModel(String name) {
        Geometry geometry = (Geometry) application.getAssetManager().loadModel(
                "models/" + name + "/" + name + ".gltf",
                GltfLoaderSettings.builder().bakeGeometries(true).build());
        if (name.equals("gaze_of_darkness") || name.equals("blade_of_chaos")) {
            geometry.setRenderBucket(RenderBucketType.TRANSPARENT);
            geometry.getMaterial().setTransparent(true);
            geometry.getMaterial().setCullMode(VK10.VK_CULL_MODE_NONE);
            geometry.getMaterial().setDepthWrite(false);
        }
        geometry.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        return geometry;
    }

    private float directionToAngle(Direction direction) {
        Vector2 vector = direction.kiloVector();
        return (float) Math.atan2(vector.y(), vector.x()) + (float) (Math.PI / 2);
    }

    private Vector3f convert(Vector2 vector) {
        float milli = 0.001f;
        return new Vector3f(vector.x() * milli, 0, -vector.y() * milli);
    }

    private static PlayerInput toInput(long player, Set<Integer> keyCodes) {
        int x = 0;
        int y = 0;
        if (keyCodes.contains(GLFW.GLFW_KEY_UP)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_7)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_8)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_9)) {
            y++;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_DOWN)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_1)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_2)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_3)) {
            y--;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_RIGHT)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_3)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_6)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_9)) {
            x++;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_LEFT)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_1)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_4)
                || keyCodes.contains(GLFW.GLFW_KEY_KP_7)) {
            x--;
        }
        Direction direction = Direction.eightDirOf(x, y);
        ActionType action = ActionType.IDLE;
        if (direction != null) {
            action = ActionType.WALK;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_X)
                || keyCodes.contains(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            action = ActionType.DASH;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_1)
                || keyCodes.contains(GLFW.GLFW_KEY_Q)
                || keyCodes.contains(GLFW.GLFW_KEY_A)) {
            action = ActionType.ATTACK1;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_2)
                || keyCodes.contains(GLFW.GLFW_KEY_W)
                || keyCodes.contains(GLFW.GLFW_KEY_S)) {
            action = ActionType.ATTACK2;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_3)
                || keyCodes.contains(GLFW.GLFW_KEY_E)
                || keyCodes.contains(GLFW.GLFW_KEY_D)) {
            action = ActionType.ATTACK3;
        }
        return new PlayerInput(player, direction, action);
    }

}
