package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.matrix.Matrix4f;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<>();
    public ArrayList<Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();

    private Vector3f scale = new Vector3f(1, 1, 1);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private Vector3f translation = new Vector3f(0, 0, 0);

    private Matrix4f rotationMatrix = new Matrix4f().unit(); // Единичная матрица по умолчанию

    public void setRotationMatrix(Matrix4f rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }

    public Matrix4f getRotationMatrix() {
        return rotationMatrix;
    }

    private String name = "Unnamed Model";

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void removeVertices(List<Integer> vertexIndices) {
        // Удаляем вершины в обратном порядке, чтобы индексы не сбивались
        for (int i = vertexIndices.size() - 1; i >= 0; i--) {
            int vertexIndex = vertexIndices.get(i);
            removeVertexAndUpdatePolygons(vertexIndex);
        }
    }
    public void removeVertexAndUpdatePolygons(int vertexIndexToRemove) {
        if (vertexIndexToRemove < 0 || vertexIndexToRemove >= vertices.size()) {
            throw new IllegalArgumentException("Invalid vertex index to remove");
        }
        vertices.remove(vertexIndexToRemove);
        updatePolygonIndicesAfterVertexRemoval(vertexIndexToRemove);
    }

    private void updatePolygonIndicesAfterVertexRemoval(int removedVertexIndex) {
        // Перебираем все полигоны
        for (Polygon polygon : polygons) {
            List<Integer> updatedVertexIndices = new ArrayList<>();

            // Перебираем индексы вершин в текущем полигоне
            for (int vertexIndex : polygon.getVertexIndices()) {
                if (vertexIndex < removedVertexIndex) {
                    // Если индекс меньше удаленного, он остается без изменений
                    updatedVertexIndices.add(vertexIndex);
                } else if (vertexIndex > removedVertexIndex) {
                    // Если индекс больше удаленного, уменьшаем его на 1
                    updatedVertexIndices.add(vertexIndex - 1);
                }
                // Если индекс равен удаленному, он пропускается
            }

            // Устанавливаем обновленные индексы в полигон
            polygon.setVertexIndices(updatedVertexIndices);
        }

        // Удаляем полигоны, которые стали невалидными (например, содержат менее 3 вершин)
        polygons.removeIf(polygon -> polygon.getVertexIndices().size() < 3);
    }

    private Color staticColor = Color.LIGHTGRAY; // По умолчанию

    public Color getStaticColor() {
        return staticColor;
    }

    public void setStaticColor(Color staticColor) {
        this.staticColor = staticColor;
    }
}