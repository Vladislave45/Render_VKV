package com.cgvsu.objwriter;

import com.cgvsu.math.Vector4f;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;

import java.util.ArrayList;

public class ObjWriter {

    public static String write(Model model) {
        return write(model, true);
    }

    public static String write(Model model, boolean applyTransformations) {
        StringBuilder sb = new StringBuilder();

        ArrayList<Vector3f> vertices = model.vertices;
        ArrayList<Polygon> polygons = model.polygons;

        if (applyTransformations) {
            for (Vector3f vertex : vertices) {
                Vector3f transformedVertex = GraphicConveyor.applyTransformations(vertex, model.getScale(), model.getRotation(), model.getTranslation());
                sb.append("v ").append(transformedVertex.getX()).append(" ").append(transformedVertex.getY()).append(" ").append(transformedVertex.getZ()).append("\n");
            }
        } else {
            for (Vector3f vertex : vertices) {
                sb.append("v ").append(vertex.getX()).append(" ").append(vertex.getY()).append(" ").append(vertex.getZ()).append("\n");
            }
        }

        for (Polygon polygon : polygons) {
            sb.append("f");
            for (Integer vertexIndex : polygon.getVertexIndices()) {
                sb.append(" ").append(vertexIndex + 1);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

}
