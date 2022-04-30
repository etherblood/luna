package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.Shader;
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
import com.etherblood.luna.engine.actions.data.ActionKey;
import com.etherblood.luna.engine.damage.MilliHealth;
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
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;

public class GameSystem extends LifecycleObject implements InputLayer {

    private final float CAMERA_DISTANCE = 10;
    private final float CAMERA_ANGLE = (float) (30 * Math.PI / 180);

    private final Set<Integer> pressedKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<Integer, ModelWrapper> models = new HashMap<>();
    private final Map<Integer, StatusHudWrapper> statusHuds = new HashMap<>();
    private Geometry geometryGround;
    private final GameProxy gameProxy;
    private UUID loadedGame = null;
    private BitmapFont bitmapFont;

    public GameSystem(GameProxy gameProxy) {
        this.gameProxy = gameProxy;
    }

    @Override
    public int orderNumber() {
        return LayerOrder.GAME;
    }

    @Override
    protected void init() {
        try (PrintStopwatch stopwatch = new PrintStopwatch("init")) {
            super.init();

            GLFW.glfwSetWindowTitle(application.getWindow(), gameProxy.getPlayer().login);
            application.getAssetManager().addLocator(new FileLocator("./assets"));

            DirectionalLight directionalLight = new DirectionalLight();
            directionalLight.getAmbientColor().set(0.75f, 0.75f, 0.75f, 1);
            directionalLight.getLightColor().set(0.75f, 0.75f, 0.75f, 1);
            directionalLight.setDirection(new Vector3f(0, -1, 0).normalize());
            directionalLight.addAffectedSpatial(application.getSceneNode());
            directionalLight.addShadows(4096);
            application.setLight(directionalLight);

            application.getSceneCamera().setLocation(new Vector3f(0, 2, 10));
            application.getSceneCamera().setRotation(new Quaternionf(new AxisAngle4f(CAMERA_ANGLE, 1, 0, 0)));

            // text
            bitmapFont = application.getAssetManager().loadBitmapFont("fonts/Verdana_18.fnt");


            // Ground
            Quad meshGround = new Quad(10, 10);

            Material materialGround = new Material();
            materialGround.setVertexShader(new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[]{
                    "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                    "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
            }));
            materialGround.setFragmentShader(new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[]{
                    "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                    "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
            }));
            materialGround.getParameters().setVector4f("color", new Vector4f(0.2f, 0.2f, 0.2f, 1));

            geometryGround = new Geometry();
            geometryGround.setMesh(meshGround);
            geometryGround.setMaterial(materialGround);
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
        return true;
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
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
        Iterator<StatusHudWrapper> iterator = statusHuds.values().iterator();
        while (iterator.hasNext()) {
            StatusHudWrapper wrapper = iterator.next();
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
                StatusHudWrapper wrapper = new StatusHudWrapper(entity, bitmapFont);
                statusHuds.put(entity, wrapper);
                guiNode.add(wrapper.getNode());
            }
            Vector2 position = data.get(entity, Position.class).vector();
            ActorName actorName = data.get(entity, ActorName.class);
            String name = actorName == null ? null : actorName.name();
            MilliHealth health = data.get(entity, MilliHealth.class);
            StatusHudWrapper wrapper = statusHuds.get(entity);
            wrapper.setName(name);
            wrapper.setHealth(health == null ? null : health.value());
            Vector3f screenCoordinates = application.getScreenCoordinates(convert(position));
            wrapper.getNode().setLocalTranslation(screenCoordinates);
        }
    }

    private void updateModels(GameEngine snapshot) {
        Node sceneNode = application.getSceneNode();
        EntityData data = snapshot.getData();
        Iterator<ModelWrapper> iterator = models.values().iterator();
        while (iterator.hasNext()) {
            ModelWrapper wrapper = iterator.next();
            if (!data.has(wrapper.getEntity(), ModelKey.class)) {
                if (wrapper.getNode().hasParent(sceneNode)) {
                    sceneNode.remove(wrapper.getNode());
                }
                iterator.remove();
            }
        }

        for (int entity : data.list(ModelKey.class)) {
            String name = data.get(entity, ModelKey.class).name();
            if (!models.containsKey(entity)) {
                Geometry model = loadModel(name);
                models.put(entity, new ModelWrapper(entity, model));
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

            Direction direction = data.get(entity, Direction.class);
            if (direction != null) {
                float angle = directionToAngle(direction);
                AxisAngle4f axisAngle = new AxisAngle4f(angle, 0, 1, 0);
                wrapper.getNode().setLocalRotation(new Quaternionf(axisAngle));
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
                    wrapper.setAnimationProgress(animation, progress);
                } else {
                    wrapper.setAnimationTime(animation, animationFrames / fps);
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
                try (PrintStopwatch sub = new PrintStopwatch("update render dependencies")) {
                    Node sceneNode = application.getSceneNode();
                    sceneNode.add(preloadNode);
                    application.updateRenderDependencies();
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
        ActionKey action = ActionKey.IDLE;
        if (direction != null) {
            action = ActionKey.WALK;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_X)
                || keyCodes.contains(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            action = ActionKey.DASH;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_1)
                || keyCodes.contains(GLFW.GLFW_KEY_Q)
                || keyCodes.contains(GLFW.GLFW_KEY_A)) {
            action = ActionKey.ATTACK1;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_2)
                || keyCodes.contains(GLFW.GLFW_KEY_W)
                || keyCodes.contains(GLFW.GLFW_KEY_S)) {
            action = ActionKey.ATTACK2;
        }
        if (keyCodes.contains(GLFW.GLFW_KEY_3)
                || keyCodes.contains(GLFW.GLFW_KEY_E)
                || keyCodes.contains(GLFW.GLFW_KEY_D)) {
            action = ActionKey.ATTACK3;
        }
        return new PlayerInput(player, direction, action);
    }

}
