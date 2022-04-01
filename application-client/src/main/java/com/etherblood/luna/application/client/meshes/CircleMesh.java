package com.etherblood.luna.application.client.meshes;

import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.mesh.Mesh;
import org.joml.Vector3f;

public class CircleMesh extends Mesh {

    public CircleMesh(Vector3f center, float radius, int vertexCount) {
        vertices = new VertexData[vertexCount + 1];
        for (int i = 0; i < vertices.length; i++) {
            Vector3f vector = new Vector3f(radius, 0, 0).rotateY((float) (2 * Math.PI * i / vertexCount));
            VertexData data = new VertexData();
            data.setVector3f("vertexPosition", vector.add(center, new Vector3f()));
            data.setVector3f("vertexNormal", new Vector3f(0, 1, 0));
            vertices[i] = data;
        }
        VertexData data = new VertexData();
        data.setVector3f("vertexPosition", new Vector3f(0).add(center, new Vector3f()));
        data.setVector3f("vertexNormal", new Vector3f(0, 1, 0));
        vertices[vertexCount] = data;

        indices = new int[3 * vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            indices[3 * i + 0] = i;
            indices[3 * i + 1] = (i + 1) % vertexCount;
            indices[3 * i + 2] = vertexCount;
        }
    }
}
