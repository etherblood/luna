package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.camera.systems.CameraKeyMoveSystem;
import com.destrostudios.icetea.core.camera.systems.CameraMouseRotateSystem;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Box;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.Shader;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Speed;
import com.etherblood.luna.engine.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_LINE;

public class ApplicationClient extends Application {

    private final Set<Integer> pressedKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Material materialCool;
    private Geometry geometryGround;
    private Node nodeAmara;
    private Node nodeAmaraWrapper;
    private Geometry geometryBounds;
    private Node nodeCollisions;
    private boolean rotateObjects = true;
    private final GameProxy gameProxy;
    private final List<String> animations = List.of("attack1", "attack2", "idle", "walk");
    AtomicInteger animationIndex = new AtomicInteger(animations.indexOf("idle"));

    private long runningFrameSecond;
    private int runningFrameCount;
    private int frameCount;

    public ApplicationClient(GameProxy gameProxy) {
        this.gameProxy = gameProxy;
    }

    @Override
    protected void initScene() {
        long nanos = System.nanoTime();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 5, 20));

        Shader vertexShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });
        Shader fragShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[]{
                "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
                "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });
        Shader vertexShaderCool = new Shader("shaders/veryCool.vert");
        Shader fragShaderCool = new Shader("shaders/veryCool.frag", new String[]{
                "shaders/nodes/texCoordColor.glsllib",
                "shaders/nodes/alphaPulsate.glsllib"
        });
        materialCool = new Material();
        materialCool.setVertexShader(vertexShaderCool);
        materialCool.setFragmentShader(fragShaderCool);
        materialCool.setTransparent(true);


        // Ground

        Quad meshGround = new Quad(10, 10);

        Material materialGround = new Material();
        materialGround.setVertexShader(vertexShaderDefault);
        materialGround.setFragmentShader(fragShaderDefault);
        materialGround.getParameters().setVector4f("color", new Vector4f(0.2f, 0.2f, 0.2f, 1));

        geometryGround = new Geometry();
        geometryGround.setMesh(meshGround);
        geometryGround.setMaterial(materialGround);
        geometryGround.move(new Vector3f(-5, -0.25f, 5));
        geometryGround.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryGround.setShadowMode(ShadowMode.RECEIVE);
        sceneNode.add(geometryGround);

        // Amara
        nodeAmara = (Node) assetManager.loadModel("models/amara/amara.gltf");
        nodeAmara.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        nodeAmara.setShadowMode(ShadowMode.CAST);

        AnimationControl a = (AnimationControl) nodeAmara.getControls().iterator().next();
        System.out.println(a.getAnimations().length);
        if (a.getAnimations().length != 0) {
            a.play(animations.indexOf("idle"));
        }

        nodeAmaraWrapper = new Node();
        nodeAmaraWrapper.add(nodeAmara);
        nodeAmaraWrapper.move(new Vector3f(1, -0.25f, 1.5f));
        sceneNode.add(nodeAmaraWrapper);

        // Bounds

        Mesh meshBox = new Box(1, 1, 1);

        Material materialBounds = new Material();
        materialBounds.setVertexShader(vertexShaderDefault);
        materialBounds.setFragmentShader(fragShaderDefault);
        materialBounds.setCullMode(VK_CULL_MODE_NONE);
        materialBounds.setFillMode(VK_POLYGON_MODE_LINE);
        materialBounds.getParameters().setVector4f("color", new Vector4f(1, 0, 0, 1));

        geometryBounds = new Geometry();
        geometryBounds.setMesh(meshBox);
        geometryBounds.setMaterial(materialBounds);
        sceneNode.add(geometryBounds);

        // Collisions

        nodeCollisions = new Node();
        sceneNode.add(nodeCollisions);

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
//                case GLFW_KEY_LEFT:
//                    if (keyEvent.getAction() == GLFW_PRESS) {
//                        a.play(Math.floorMod(animationIndex.decrementAndGet(), a.getAnimations().length));
//                        System.out.println(animationIndex.get());
//                    }
//                    break;
//                case GLFW_KEY_RIGHT:
//                    if (keyEvent.getAction() == GLFW_PRESS) {
//                        a.play(Math.floorMod(animationIndex.incrementAndGet(), a.getAnimations().length));
//                        System.out.println(animationIndex.get());
//                    }
//                    break;
                case GLFW.GLFW_KEY_1:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        System.out.println("fps: " + frameCount);
                    }
                    break;
                case GLFW.GLFW_KEY_6:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        rotateObjects = !rotateObjects;
                    }
                    break;
                case GLFW.GLFW_KEY_7:
                    if (keyEvent.getAction() == GLFW.GLFW_PRESS) {
                        nodeAmara.setShadowMode((nodeAmara.getShadowMode() == ShadowMode.INHERIT) ? ShadowMode.CAST : ShadowMode.INHERIT);
                    }
                    break;
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
                Vector3f worldCoordinatesFront = getWorldCoordinates(sceneCamera, inputManager.getCursorPosition(), 0);
                Vector3f worldCoordinatesBack = getWorldCoordinates(sceneCamera, inputManager.getCursorPosition(), 1);
                Vector3f rayDirection = worldCoordinatesBack.sub(worldCoordinatesFront, new Vector3f());
                Ray ray = new Ray(worldCoordinatesFront, rayDirection);

                ArrayList<CollisionResult> collisionResults = new ArrayList<>();
                if (mouseButtonEvent.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    sceneNode.collideStatic(ray, collisionResults);
                } else {
                    sceneNode.collideDynamic(ray, collisionResults);
                }

                LinkedList<Geometry> displayedCollisions = new LinkedList<>();
                for (CollisionResult collisionResult : collisionResults) {
                    if (collisionResult.getGeometry().getParent() != nodeCollisions) {
                        Geometry geometryBox = new Geometry();
                        geometryBox.setMesh(meshBox);

                        Material materialBox = new Material();
                        materialBox.setVertexShader(vertexShaderDefault);
                        materialBox.setFragmentShader(fragShaderDefault);
                        geometryBox.setMaterial(materialBox);

                        float boxSize = 0.05f;
                        geometryBox.setLocalTranslation(collisionResult.getPosition().sub((boxSize / 2), (boxSize / 2), (boxSize / 2), new Vector3f()));
                        geometryBox.setLocalScale(new Vector3f(boxSize, boxSize, boxSize));
                        displayedCollisions.add(geometryBox);
                    }
                }

                nodeCollisions.removeAll();
                for (Geometry displayedCollision : displayedCollisions) {
                    nodeCollisions.add(displayedCollision);
                }
            }
        });
        System.out.println((System.nanoTime() - nanos) / 1_000_000 + "ms");
    }

    @Override
    protected void update(float tpf) {
        if (rotateObjects) {
            nodeAmara.rotate(new Quaternionf().rotateLocalY((float) (-0.1f * Math.PI * tpf)));
        }
        EntityData data = gameProxy.getEngine().getData();
        int player = gameProxy.getPlayer();
        List<Integer> characters = data.findByValue(new OwnedBy(player));
        int nextAnimation = animations.indexOf("idle");
        for (Integer character : characters) {
            Position position = data.get(character, Position.class);
            if (position != null) {
                Vector2 vector = position.vector();
                nodeAmara.setLocalTranslation(convert(vector));
            }
            Direction direction = data.get(character, Direction.class);
            if (direction != null) {
                float angle1 = directionToAngle(direction);
//                System.out.println(angle1);
                AxisAngle4f angle = new AxisAngle4f(angle1, 0, 1, 0);
                nodeAmara.setLocalRotation(new Quaternionf(angle));
            }
            Speed speed = data.get(character, Speed.class);
            if (speed != null) {
                if (speed.vector().x() != 0 || speed.vector().y() != 0) {
                    nextAnimation = animations.indexOf("walk");
                }
            }
        }
        if (nextAnimation != animationIndex.get()) {
            animationIndex.set(nextAnimation);
            AnimationControl a = (AnimationControl) nodeAmara.getControls().iterator().next();
            a.play(nextAnimation);
        }
        gameProxy.requestInput(toInput(pressedKeys));
        gameProxy.tick();// TODO: this needs to happen at a fixed framerate and should be taken care of externally

        long frameSecond = Math.floorDiv(System.nanoTime(), 1_000_000_000L);
        if (runningFrameSecond != frameSecond) {
            frameCount = runningFrameCount;
            runningFrameCount = 0;
            runningFrameSecond = frameSecond;
        }
        runningFrameCount++;
    }

    private float directionToAngle(Direction direction) {
        Vector2 vector = direction.toLengthVector(1_000_000);
        return (float) Math.atan2(-vector.y(), vector.x()) + (float) (Math.PI / 2);
    }

    private Vector3f convert(Vector2 vector) {
        float milli = 0.0001f;
        return new Vector3f(vector.x() * milli, 0, vector.y() * milli);
    }

    private static Set<Object> toInput(Set<Integer> keyCodes) {
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
        return Set.of(new PlayerInput(Direction.of(x, y), ActorAction.IDLE));
    }

}
