package com.cgvsu.utils;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class NormalUtils {

    //Пересчитывает нормали вершин на основе нормалей полигонов.
    public static List<Vector3f> recalculateVertexNormals(Model model) {
        List<Vector3f> vertexNormals = new ArrayList<>();
        for (int i = 0; i < model.vertices.size(); i++) {
            vertexNormals.add(new Vector3f(0, 0, 0));
        }

        // Пересчитываем нормали вершин
        for (Polygon polygon : model.polygons) {
            Vector3f polygonNormal = computePolygonNormal(polygon, model.vertices);
            for (int vertexIndex : polygon.getVertexIndices()) {
                vertexNormals.get(vertexIndex).add(polygonNormal); // Суммируем нормали полигонов
            }
        }

        // Нормализуем нормали вершин
        for (Vector3f normal : vertexNormals) {
            normal.normalize();
        }

        return vertexNormals;
    }

    //Вычисляет нормаль полигона.
    static Vector3f computePolygonNormal(Polygon polygon, List<Vector3f> vertices) {
        List<Integer> vertexIndices = polygon.getVertexIndices();
        if (vertexIndices.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 vertices.");
        }

        Vector3f v1 = vertices.get(vertexIndices.get(0));
        Vector3f v2 = vertices.get(vertexIndices.get(1));
        Vector3f v3 = vertices.get(vertexIndices.get(2));

        Vector3f edge1 = Vector3f.deduct(v2, v1);
        Vector3f edge2 = Vector3f.deduct(v3, v1);

        return Vector3f.crossProduct(edge1, edge2).normalize();
    }
}