package com.cgvsu.utils.triangles_utils;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.utils.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class TriangleRasterization {

    public static void drawTriangle(
            GraphicsContext gc,
            ArrayList<Vector2f> triangle,
            Image texture, // Добавляем текстуру
            Vector2f t1, Vector2f t2, Vector2f t3, // Текстурные координаты
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

            Vector2f tempTex = t1;
            t1 = t2;
            t2 = tempTex;
        }
        if (v2.getY() > v3.getY()) {
            Vector2f temp = v2;
            v2 = v3;
            v3 = temp;

            Vector2f tempTex = t2;
            t2 = t3;
            t3 = tempTex;
        }
        if (v1.getY() > v2.getY()) {
            Vector2f temp = v1;
            v1 = v2;
            v2 = temp;

            Vector2f tempTex = t1;
            t1 = t2;
            t2 = tempTex;
        }

        // Растеризация треугольника
        rasterizeTriangle(gc, v1, v2, v3, v1_3d, v2_3d, v3_3d, zBuffer, texture, t1, t2, t3);
    }
    private static float interpolateTexture(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3, float t1, float t2, float t3) {
        float w1 = ((v2.getY() - v3.getY()) * (x - v3.getX()) + (v3.getX() - v2.getX()) * (y - v3.getY())) /
                ((v2.getY() - v3.getY()) * (v1.getX() - v3.getX()) + (v3.getX() - v2.getX()) * (v1.getY() - v3.getY()));
        float w2 = ((v3.getY() - v1.getY()) * (x - v3.getX()) + (v1.getX() - v3.getX()) * (y - v3.getY())) /
                ((v2.getY() - v3.getY()) * (v1.getX() - v3.getX()) + (v3.getX() - v2.getX()) * (v1.getY() - v3.getY()));
        float w3 = 1 - w1 - w2;

        return w1 * t1 + w2 * t2 + w3 * t3;
    }

    private static Color getTextureColor(Image texture, float u, float v) {
        int x = (int) (u * texture.getWidth());
        int y = (int) (v * texture.getHeight());

        // Ограничиваем координаты текстуры в пределах изображения
        x = (int) Math.max(0, Math.min(texture.getWidth() - 1, x));
        y = (int) Math.max(0, Math.min(texture.getHeight() - 1, y));

        return texture.getPixelReader().getColor(x, y);
    }
    private static void rasterizeTriangle(
            GraphicsContext gc,
            Vector2f v1, Vector2f v2, Vector2f v3,
            Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d,
            ArrayList<ArrayList<Float>> zBuffer,
            Image texture, // Добавляем текстуру
            Vector2f t1, Vector2f t2, Vector2f t3 // Текстурные координаты
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
                        // Интерполяция текстурных координат
                        float u = interpolateTexture(x, y, v1, v2, v3, t1.getX(), t2.getX(), t3.getX());
                        float v = interpolateTexture(x, y, v1, v2, v3, t1.getY(), t2.getY(), t3.getY());

                        // Обрезаем текстурные координаты в диапазон [0, 1]
                        u = Math.max(0, Math.min(1, u));
                        v = Math.max(0, Math.min(1, v));

                        // Получаем цвет из текстуры
                        Color texColor = getTextureColor(texture, u, v);

                        // Устанавливаем цвет пикселя
                        gc.getPixelWriter().setColor(x, y, texColor);
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