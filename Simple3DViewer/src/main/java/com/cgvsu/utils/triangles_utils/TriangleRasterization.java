package com.cgvsu.utils.triangles_utils;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.utils.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class TriangleRasterization {

    public static void drawTriangle(
            GraphicsContext gc,
            ArrayList<Vector2f> triangle,
            Color color1, Color color2, Color color3,
            ArrayList<ArrayList<Float>> zBuffer,
            Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d
    ) {
        Vector2f v1 = triangle.get(0);
        Vector2f v2 = triangle.get(1);
        Vector2f v3 = triangle.get(2);

        // Сортируем вершины по Y
        if (v1.getY() > v2.getY()) {
            Vector2f temp = v1;
            v1 = v2;
            v2 = temp;

            Vector3f temp3d = v1_3d;
            v1_3d = v2_3d;
            v2_3d = temp3d;
        }
        if (v2.getY() > v3.getY()) {
            Vector2f temp = v2;
            v2 = v3;
            v3 = temp;

            Vector3f temp3d = v2_3d;
            v2_3d = v3_3d;
            v3_3d = temp3d;
        }
        if (v1.getY() > v2.getY()) {
            Vector2f temp = v1;
            v1 = v2;
            v2 = temp;

            Vector3f temp3d = v1_3d;
            v1_3d = v2_3d;
            v2_3d = temp3d;
        }

        // Растеризация треугольника
        rasterizeTriangle(gc, v1, v2, v3, v1_3d, v2_3d, v3_3d, color1, zBuffer);
    }

    private static void rasterizeTriangle(
            GraphicsContext gc,
            Vector2f v1, Vector2f v2, Vector2f v3,
            Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d,
            Color color,
            ArrayList<ArrayList<Float>> zBuffer
    ) {
        // Алгоритм растеризации треугольника с учётом Z-буфера
        int minX = (int) Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()));
        int maxX = (int) Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()));
        int minY = (int) Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()));
        int maxY = (int) Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInTriangle(x, y, v1, v2, v3)) {
                    float depth = interpolateDepth(x, y, v1, v2, v3, v1_3d, v2_3d, v3_3d);

                    // Проверяем Z-буфер
                    if (ZBuffer.testBuffer(x, y, depth, zBuffer)) {
                        gc.getPixelWriter().setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static boolean isPointInTriangle(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3) {
        float d1 = sign(x, y, v1, v2);
        float d2 = sign(x, y, v2, v3);
        float d3 = sign(x, y, v3, v1);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private static float sign(int x, int y, Vector2f v1, Vector2f v2) {
        return (x - v2.getX()) * (v1.getY() - v2.getY()) - (v1.getX() - v2.getX()) * (y - v2.getY());
    }

    private static float interpolateDepth(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3, Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d) {
        float w1 = ((v2.getY() - v3.getY()) * (x - v3.getX()) + (v3.getX() - v2.getX()) * (y - v3.getY())) /
                ((v2.getY() - v3.getY()) * (v1.getX() - v3.getX()) + (v3.getX() - v2.getX()) * (v1.getY() - v3.getY()));
        float w2 = ((v3.getY() - v1.getY()) * (x - v3.getX()) + (v1.getX() - v3.getX()) * (y - v3.getY())) /
                ((v2.getY() - v3.getY()) * (v1.getX() - v3.getX()) + (v3.getX() - v2.getX()) * (v1.getY() - v3.getY()));
        float w3 = 1 - w1 - w2;

        return w1 * v1_3d.getZ() + w2 * v2_3d.getZ() + w3 * v3_3d.getZ();
    }

    public static void drawTriangle(GraphicsContext gc, ArrayList<Vector2f> points, Color red, Color green, Color blue) {
    }
}