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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_LINE;

public class ApplicationClient extends Application {

    private Material materialCool;
    private Node nodeRotating;
    private Geometry geometryGround;
    private Node nodeDuck;
    private Geometry geometryBounds;
    private Node nodeCollisions;
    private boolean rotateObjects = true;

    @Override
    protected void initScene() {
        long nanos = System.nanoTime();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 0.3f, 5));

        nodeRotating = new Node();
        sceneNode.add(nodeRotating);

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
        materialGround.getParameters().setVector4f("color", new Vector4f(1, 1, 1, 1));

        geometryGround = new Geometry();
        geometryGround.setMesh(meshGround);
        geometryGround.setMaterial(materialGround);
        geometryGround.move(new Vector3f(-5, -0.25f, 5));
        geometryGround.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryGround.setShadowMode(ShadowMode.RECEIVE);

        // Duck

        nodeDuck = (Node) assetManager.loadModel("models/amara/amara.gltf");
        nodeDuck.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        nodeDuck.setShadowMode(ShadowMode.CAST);

        AnimationControl a = (AnimationControl) nodeDuck.getControls().iterator().next();
        System.out.println(a.getAnimations().length);
        if (a.getAnimations().length != 0) {
            a.play(0);
        }

        Node nodeDuckWrapper = new Node();
        nodeDuckWrapper.add(nodeDuck);
        nodeDuckWrapper.move(new Vector3f(1, -0.25f, 1.5f));
        nodeRotating.add(nodeDuckWrapper);

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

        AtomicInteger animationIndex = new AtomicInteger();

        CameraMouseRotateSystem cameraMouseRotateSystem = new CameraMouseRotateSystem(sceneCamera);
        CameraKeyMoveSystem cameraKeyMoveSystem = new CameraKeyMoveSystem(sceneCamera);
        inputManager.addKeyListener(keyEvent -> {
            // Add/Remove filters
            switch (keyEvent.getKey()) {
                case GLFW_KEY_LEFT:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        a.play(Math.floorMod(animationIndex.decrementAndGet(), a.getAnimations().length));
                        System.out.println(animationIndex.get());
                    }
                    break;
                case GLFW_KEY_RIGHT:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        a.play(Math.floorMod(animationIndex.incrementAndGet(), a.getAnimations().length));
                        System.out.println(animationIndex.get());
                    }
                    break;
                case GLFW_KEY_4:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (geometryGround.getParent() == sceneNode) {
                            sceneNode.remove(geometryGround);
                        } else {
                            sceneNode.add(geometryGround);
                        }
                    }
                    break;
                case GLFW_KEY_6:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        rotateObjects = !rotateObjects;
                    }
                    break;
                case GLFW_KEY_7:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        nodeDuck.setShadowMode((nodeDuck.getShadowMode() == ShadowMode.INHERIT) ? ShadowMode.CAST : ShadowMode.INHERIT);
                    }
                    break;
                case GLFW_KEY_9:
                    if (keyEvent.getAction() == GLFW_PRESS) {
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
            if (mouseButtonEvent.getAction() == GLFW_PRESS) {
                Vector3f worldCoordinatesFront = getWorldCoordinates(sceneCamera, inputManager.getCursorPosition(), 0);
                Vector3f worldCoordinatesBack = getWorldCoordinates(sceneCamera, inputManager.getCursorPosition(), 1);
                Vector3f rayDirection = worldCoordinatesBack.sub(worldCoordinatesFront, new Vector3f());
                Ray ray = new Ray(worldCoordinatesFront, rayDirection);

                ArrayList<CollisionResult> collisionResults = new ArrayList<>();
                if (mouseButtonEvent.getButton() == GLFW_MOUSE_BUTTON_LEFT) {
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
        nodeDuck.rotate(new Quaternionf().rotateLocalY((float) (-0.1f * Math.PI * tpf)));
    }
}
