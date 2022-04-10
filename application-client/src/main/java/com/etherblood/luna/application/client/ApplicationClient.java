package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.application.client.meshes.CircleMesh;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActiveAction;
import com.etherblood.luna.engine.ActorName;
import com.etherblood.luna.engine.Circle;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.ModelKey;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.data.ActionAnimation;
import com.etherblood.luna.engine.actions.data.ActionDuration;
import com.etherblood.luna.engine.actions.data.ActionKey;
import com.etherblood.luna.engine.damage.Damagebox;
import com.etherblood.luna.engine.damage.Hitbox;
import com.etherblood.luna.engine.damage.MilliHealth;
import com.etherblood.luna.engine.movement.Obstaclebox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.lwjgl.vulkan.VK10;

public class ApplicationClient extends Application {

    private final float CAMERA_DISTANCE = 10;
    private final float CAMERA_LOOKAT_HEIGHT = 0f;
    private final float CAMERA_ANGLE = (float) (30 * Math.PI / 180);

    private final Set<Integer> pressedKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<Integer, ModelWrapper> models = new HashMap<>();
    private final Map<Integer, HitboxWrapper> hitboxes = new HashMap<>();
    private final Map<Integer, HitboxWrapper> damageboxes = new HashMap<>();
    private final Map<Integer, HitboxWrapper> obstacleboxes = new HashMap<>();
    private final Map<Integer, StatusHudWrapper> statusHuds = new HashMap<>();
    private Geometry geometryGround;
    private final GameProxy gameProxy;

    private BitmapFont bitmapFont;
    private BitmapText screenStatsText;
    private long runningFrameSecond;
    private int runningFrameCount;
    private int frameCount;

    Shader vertexShaderDefault;
    Shader fragShaderDefault;
    Node debugNode = new Node();

    public ApplicationClient(GameProxy gameProxy) {
        super();
        setPreferredPresentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
        this.gameProxy = gameProxy;
    }

    @Override
    protected void initScene() {
        long nanos = System.nanoTime();
        getSwapChain().getRenderJobManager().getSceneRenderJob().getClearColor().set(0.2f, 0.15f, 0.15f, 1);
        GLFW.glfwSetWindowTitle(getWindow(), gameProxy.getPlayer().login);
        assetManager.addLocator(new FileLocator("./assets"));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.getAmbientColor().set(0.8f);
        directionalLight.setDirection(new Vector3f(0, -1, 0).normalize());
        directionalLight.addAffectedSpatial(sceneNode);
        directionalLight.addShadows(4096);
        setLight(directionalLight);

        sceneCamera.setLocation(new Vector3f(0, 2, 10));
        sceneCamera.setRotation(new Quaternionf(new AxisAngle4f(CAMERA_ANGLE, 1, 0, 0)));

        vertexShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });
        fragShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });

        // text

        bitmapFont = assetManager.loadBitmapFont("fonts/Verdana_18.fnt");

        screenStatsText = new BitmapText(bitmapFont, "Connecting...");
        screenStatsText.move(new Vector3f(0, 0, 1));
        guiNode.add(screenStatsText);


        // Ground

        Quad meshGround = new Quad(10, 30);

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

        inputManager.addKeyListener(keyEvent -> {
            if (keyEvent.getAction() == GLFW.GLFW_RELEASE) {
                pressedKeys.remove(keyEvent.getKey());
            } else {
                pressedKeys.add(keyEvent.getKey());
            }
            if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                switch (keyEvent.getKey()) {
                    case GLFW.GLFW_KEY_F1:
                        if (debugNode.hasParent(sceneNode)) {
                            sceneNode.remove(debugNode);
                        } else {
                            sceneNode.add(debugNode);
                        }
                        break;
                }
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
        if (snapshot == null) {
            // game has not started yet, skip update
            // we use this opportunity to preload amara
            assetManager.loadModel("models/amara/amara.gltf", CloneContext.reuseAll());
            return;
        }
        EntityData data = snapshot.getData();

        // TODO: cleanup spaghetti

        // camera
        List<Integer> players = data.findByValue(new PlayerId(gameProxy.getPlayer().id));
        for (int player : players) {
            Position position = data.get(player, Position.class);
            if (position != null) {
                Vector3f lookAt = convert(position.vector()).add(0, CAMERA_LOOKAT_HEIGHT, 0);
                Vector3f cameraOffset = new Vector3f(0, 0, CAMERA_DISTANCE);
                cameraOffset.rotate(new Quaternionf(new AxisAngle4f(-CAMERA_ANGLE, 1, 0, 0)));
                Vector3f lookFrom = lookAt.add(cameraOffset, new Vector3f());
                sceneCamera.setLocation(lookFrom);
            }
        }

        {
            // status huds
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
                Vector3f screenCoordinates = getScreenCoordinates(convert(position));
                wrapper.getNode().setLocalTranslation(screenCoordinates);
            }
        }

        {
            // obstacleboxes
            Iterator<HitboxWrapper> iterator = obstacleboxes.values().iterator();
            while (iterator.hasNext()) {
                HitboxWrapper wrapper = iterator.next();
                if (!data.has(wrapper.getEntity(), Obstaclebox.class)) {
                    debugNode.remove(wrapper.getNode());
                    iterator.remove();
                }
            }
            for (int entity : data.list(Obstaclebox.class)) {
                Rectangle shape = data.get(entity, Obstaclebox.class).shape();
                if (!obstacleboxes.containsKey(entity)) {

                    Mesh quad = new Quad(shape.width() / 1000f, shape.height() / 1000f);
                    Geometry geometry = new Geometry();
                    geometry.setMesh(quad);
                    geometry.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (-Math.PI / 2), 1, 0, 0)));
                    Material material = new Material();
                    material.setVertexShader(vertexShaderDefault);
                    material.setFragmentShader(fragShaderDefault);
                    material.setCullMode(VK10.VK_CULL_MODE_NONE);
                    material.setFillMode(VK10.VK_POLYGON_MODE_LINE);
                    material.getParameters().setVector4f("color", new Vector4f(0, 0, 1, 1));
                    geometry.setMaterial(material);
                    geometry.setShadowMode(ShadowMode.OFF);
                    obstacleboxes.put(entity, new HitboxWrapper(entity, geometry));
                    debugNode.add(geometry);
                }
                Position obstaclePosition = data.get(entity, Position.class);
                Vector2 position;
                if (obstaclePosition != null) {
                    position = obstaclePosition.vector();
                } else {
                    position = new Vector2(0, 0);
                }
                obstacleboxes.get(entity).getNode().setLocalTranslation(convert(position.add(shape.x(), shape.y())));
            }
        }

        {
            // hitboxes
            Iterator<HitboxWrapper> iterator = hitboxes.values().iterator();
            while (iterator.hasNext()) {
                HitboxWrapper wrapper = iterator.next();
                if (!data.has(wrapper.getEntity(), Hitbox.class)) {
                    debugNode.remove(wrapper.getNode());
                    iterator.remove();
                }
            }
            for (int entity : data.list(Hitbox.class)) {
                if (!hitboxes.containsKey(entity)) {
                    Circle shape = data.get(entity, Hitbox.class).area();

                    CircleMesh circle = new CircleMesh(convert(new Vector2(shape.x(), shape.y())), shape.radius() / 1000f, 32);
                    Geometry geometry = new Geometry();
                    geometry.setMesh(circle);
                    Material material = new Material();
                    material.setVertexShader(vertexShaderDefault);
                    material.setFragmentShader(fragShaderDefault);
                    material.setCullMode(VK10.VK_CULL_MODE_NONE);
                    material.setFillMode(VK10.VK_POLYGON_MODE_LINE);
                    material.getParameters().setVector4f("color", new Vector4f(1, 1, 1, 1));
                    geometry.setMaterial(material);
                    geometry.setShadowMode(ShadowMode.OFF);
                    hitboxes.put(entity, new HitboxWrapper(entity, geometry));
                    debugNode.add(geometry);
                }
                Vector2 position = data.get(entity, Position.class).vector();
                hitboxes.get(entity).getNode().setLocalTranslation(convert(position));
            }
        }

        {
            // damageboxes
            Iterator<HitboxWrapper> iterator = damageboxes.values().iterator();
            while (iterator.hasNext()) {
                HitboxWrapper wrapper = iterator.next();
                if (!data.has(wrapper.getEntity(), Damagebox.class)) {
                    debugNode.remove(wrapper.getNode());
                    iterator.remove();
                }
            }
            for (int entity : data.list(Damagebox.class)) {
                if (!damageboxes.containsKey(entity)) {
                    Circle shape = data.get(entity, Damagebox.class).area();

                    CircleMesh circle = new CircleMesh(convert(new Vector2(shape.x(), shape.y())), shape.radius() / 1000f, 32);
                    Geometry geometry = new Geometry();
                    geometry.setMesh(circle);
                    Material material = new Material();
                    material.setVertexShader(vertexShaderDefault);
                    material.setFragmentShader(fragShaderDefault);
                    material.setCullMode(VK10.VK_CULL_MODE_NONE);
                    material.setFillMode(VK10.VK_POLYGON_MODE_LINE);
                    material.getParameters().setVector4f("color", new Vector4f(1, 0, 0, 1));
                    geometry.setMaterial(material);
                    geometry.setShadowMode(ShadowMode.OFF);
                    damageboxes.put(entity, new HitboxWrapper(entity, geometry));
                    debugNode.add(geometry);
                }
                Vector2 position = data.get(entity, Position.class).vector();
                damageboxes.get(entity).getNode().setLocalTranslation(convert(position));
            }
        }

        {
            // models
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
                    long nanos = System.nanoTime();
                    // hard coded amara model
                    Node model = assetManager.loadModel("models/" + name + "/" + name + ".gltf", CloneContext.reuseAll());
                    AnimationControl a = model.getFirstControl(AnimationControl.class);
                    if (a != null) {
                        // workaround for scale issue when exporting from blender with animations
                        model.scale(new Vector3f(0.01f));
                    }
                    if (name.equals("gaze_of_darkness") || name.equals("blade_of_chaos")) {
                        model.setRenderBucket(RenderBucketType.TRANSPARENT);
                        model.forEachGeometry(geometry -> {
                            geometry.getMaterial().setTransparent(true);
                            geometry.getMaterial().setCullMode(VK10.VK_CULL_MODE_NONE);
                            geometry.getMaterial().setDepthWrite(false);
                        });
                    }
                    model.setShadowMode(ShadowMode.CAST_AND_RECEIVE);

                    models.put(entity, new ModelWrapper(entity, model));
                    long loadMillis = (System.nanoTime() - nanos) / 1_000_000;
                    if (loadMillis >= 1) {
                        System.out.println("load " + name + " in: " + loadMillis + "ms");
                    }
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
                    Map<String, Double> animationSeconds = Map.of(
                            "attack1", 280 / 60d,
                            "attack2", 160 / 60d,
                            "agonizing", 698 / 60d,
                            "melee_attack", 72 / 60d,
                            "ghost_spell", 92 / 60d
                    );
                    String animation = data.get(activeAction.action(), ActionAnimation.class).animationName();
                    wrapper.setAnimation(animation);
                    float fps = snapshot.getRules().getFramesPerSecond();
                    long animationFrames = snapshot.getFrame() - activeAction.startFrame();

                    ActionDuration duration = data.get(activeAction.action(), ActionDuration.class);
                    if (animationSeconds.containsKey(animation) && duration != null) {
                        long targetFrames = duration.frames();
                        double targetSeconds = (double) targetFrames / snapshot.getRules().getFramesPerSecond();
                        double baseSeconds = animationSeconds.get(animation);
                        wrapper.setAnimationTime((float) ((baseSeconds / targetSeconds) * (animationFrames / fps)));
                    } else {
                        wrapper.setAnimationTime(animationFrames / fps);
                    }
                }
            }
        }

        long player = gameProxy.getPlayer().id;
        gameProxy.update(toInput(player, pressedKeys));

        long frameSecond = Math.floorDiv(System.nanoTime(), 1_000_000_000L);
        if (runningFrameSecond != frameSecond) {
            frameCount = runningFrameCount;
            runningFrameCount = 0;
            runningFrameSecond = frameSecond;
        }
        runningFrameCount++;
        screenStatsText.setText("fps: " + frameCount + "   ping: " + gameProxy.getLatency() + "ms");
    }

    private float directionToAngle(Direction direction) {
        Vector2 vector = direction.toLengthVector(1_000_000);
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
