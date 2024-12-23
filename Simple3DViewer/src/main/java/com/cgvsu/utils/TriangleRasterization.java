package com.cgvsu.utils;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.List;

public class TriangleRasterization {

    public static void drawTriangle(
            GraphicsContext gc,
            List<Vector2f> triangle,
            Image texture,
            Vector2f t1, Vector2f t2, Vector2f t3,
            List<List<Float>> zBuffer,
            Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d,
            List<Vector3f> vertexNormals,
            Camera camera,
            Color fillColor,
            int width,
            int height,
            boolean useLighting
    ) {
        Vector2f v1 = triangle.get(0);
        Vector2f v2 = triangle.get(1);
        Vector2f v3 = triangle.get(2);

        // Сортировка вершин по Y
        if (v1.getY() > v2.getY()) {
            Vector2f temp = v1;
            v1 = v2;
            v2 = temp;

            Vector3f temp3d = v1_3d;
            v1_3d = v2_3d;
            v2_3d = temp3d;

            Vector2f tempTex = t1;
            t1 = t2;
            t2 = tempTex;
        }
        if (v2.getY() > v3.getY()) {
            Vector2f temp = v2;
            v2 = v3;
            v3 = temp;

            Vector3f temp3d = v2_3d;
            v2_3d = v3_3d;
            v3_3d = temp3d;

            Vector2f tempTex = t2;
            t2 = t3;
            t3 = tempTex;
        }
        if (v1.getY() > v2.getY()) {
            Vector2f temp = v1;
            v1 = v2;
            v2 = temp;

            Vector3f temp3d = v1_3d;
            v1_3d = v2_3d;
            v2_3d = temp3d;

            Vector2f tempTex = t1;
            t1 = t2;
            t2 = tempTex;
        }

        // Растеризация треугольника
        int minX = (int) Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()));
        int maxX = (int) Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()));
        int minY = (int) Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()));
        int maxY = (int) Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInTriangle(x, y, v1, v2, v3)) {
                    float depth = interpolateDepth(x, y, v1, v2, v3, v1_3d, v2_3d, v3_3d);

                    if (ZBuffer.testBuffer(x, y, depth, zBuffer)) {
                        float u = interpolateTexture(x, y, v1, v2, v3, t1.getX(), t2.getX(), t3.getX());
                        float v = interpolateTexture(x, y, v1, v2, v3, t1.getY(), t2.getY(), t3.getY());

                        Vector3f normal = interpolateNormal(x, y, v1, v2, v3, vertexNormals.get(0), vertexNormals.get(1), vertexNormals.get(2)).normalize();

                        // Получаем цвет текстуры или используем цвет по умолчанию
                        Color texColor = getTextureColor(texture, u, v);
                        Color finalColor = texture == null ? Color.LIGHTGRAY : texColor;

                        // Применяем освещение, если оно включено
                        if (useLighting) {
                            finalColor = calculateLighting(normal, camera.getLightPosition(), finalColor);
                        }

                        // Устанавливаем цвет пикселя
                        gc.getPixelWriter().setColor(x, y, finalColor);
                    }
                }
            }
        }
    }

    private static void rasterizeTriangle(
            GraphicsContext gc,
            Vector2f v1, Vector2f v2, Vector2f v3,
            Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d,
            List<List<Float>> zBuffer,
            int width, int height,
            Image texture,
            Vector2f t1, Vector2f t2, Vector2f t3,
            List<Vector3f> vertexNormals,
            Camera camera,
            Color fillColor,
            boolean useLighting
    ) {
        // Сортировка вершин по Y
        if (v1.getY() > v2.getY()) {
            Vector2f temp = v1;
            v1 = v2;
            v2 = temp;

            Vector3f temp3d = v1_3d;
            v1_3d = v2_3d;
            v2_3d = temp3d;

            Vector2f tempTex = t1;
            t1 = t2;
            t2 = tempTex;
        }
        if (v2.getY() > v3.getY()) {
            Vector2f temp = v2;
            v2 = v3;
            v3 = temp;

            Vector3f temp3d = v2_3d;
            v2_3d = v3_3d;
            v3_3d = temp3d;

            Vector2f tempTex = t2;
            t2 = t3;
            t3 = tempTex;
        }
        if (v1.getY() > v2.getY()) {
            Vector2f temp = v1;
            v1 = v2;
            v2 = temp;

            Vector3f temp3d = v1_3d;
            v1_3d = v2_3d;
            v2_3d = temp3d;

            Vector2f tempTex = t1;
            t1 = t2;
            t2 = tempTex;
        }

        // Растеризация треугольника
        int minX = (int) Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()));
        int maxX = (int) Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()));
        int minY = (int) Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()));
        int maxY = (int) Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInTriangle(x, y, v1, v2, v3)) {
                    float depth = interpolateDepth(x, y, v1, v2, v3, v1_3d, v2_3d, v3_3d);

                    if (ZBuffer.testBuffer(x, y, depth, zBuffer)) {
                        float u = interpolateTexture(x, y, v1, v2, v3, t1.getX(), t2.getX(), t3.getX());
                        float v = interpolateTexture(x, y, v1, v2, v3, t1.getY(), t2.getY(), t3.getY());

                        Vector3f normal = interpolateNormal(x, y, v1, v2, v3, vertexNormals.get(0), vertexNormals.get(1), vertexNormals.get(2)).normalize();

                        Color texColor = getTextureColor(texture, u, v);
                        Color finalColor = texColor; // По умолчанию цвет текстуры

                        if (useLighting) { // Применяем освещение, если оно включено
                            finalColor = calculateLighting(normal, camera.getLightPosition(), texColor);
                        }

                        gc.getPixelWriter().setColor(x, y, finalColor);
                    }
                }
            }
        }
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
        if (texture == null) return Color.WHITE;

        // Преобразуем текстурные координаты в целочисленные координаты текстуры
        int x = (int) (u * (texture.getWidth() - 1));
        int y = (int) (v * (texture.getHeight() - 1));

        // Получаем цвет пикселя напрямую
        return texture.getPixelReader().getColor(x, y);
    }

    private static Color interpolateColor(Color c1, Color c2, float ratio) {
        float r = (float) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        float g = (float) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        float b = (float) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        float a = (float) (c1.getOpacity() * (1 - ratio) + c2.getOpacity() * ratio);

        return new Color(r, g, b, a);
    }

    private static Color calculateLighting(Vector3f normal, Vector3f lightDirection, Color texColor) {
        lightDirection = lightDirection.normalize(); // Нормализуем направление света

        float dotProduct = normal.dot(lightDirection);
        dotProduct = Math.max(0, dotProduct); // Ограничиваем значение от 0 до 1

        return new Color(
                texColor.getRed() * dotProduct,
                texColor.getGreen() * dotProduct,
                texColor.getBlue() * dotProduct,
                1.0
        );
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

        float depth = w1 * v1_3d.getZ() + w2 * v2_3d.getZ() + w3 * v3_3d.getZ();

        return depth;
    }

    private static Vector3f interpolateNormal(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3, Vector3f n1, Vector3f n2, Vector3f n3) {
        float w1 = ((v2.getY() - v3.getY()) * (x - v3.getX()) + (v3.getX() - v2.getX()) * (y - v3.getY())) /
                ((v2.getY() - v3.getY()) * (v1.getX() - v3.getX()) + (v3.getX() - v2.getX()) * (v1.getY() - v3.getY()));
        float w2 = ((v3.getY() - v1.getY()) * (x - v3.getX()) + (v1.getX() - v3.getX()) * (y - v3.getY())) /
                ((v2.getY() - v3.getY()) * (v1.getX() - v3.getX()) + (v3.getX() - v2.getX()) * (v1.getY() - v3.getY()));
        float w3 = 1 - w1 - w2;

        Vector3f interpolatedNormal = new Vector3f(
                n1.getX() * w1 + n2.getX() * w2 + n3.getX() * w3,
                n1.getY() * w1 + n2.getY() * w2 + n3.getY() * w3,
                n1.getZ() * w1 + n2.getZ() * w2 + n3.getZ() * w3
        );

        return interpolatedNormal.normalize(); // Нормализуем интерполированную нормаль
    }
}