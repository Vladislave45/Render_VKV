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

    private static float edgeFunction(Vector2f a, Vector2f b, Vector2f c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    private static float interpolateTexture(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3, float t1, float t2, float t3) {
        float area = edgeFunction(v1, v2, v3);
        float w1 = edgeFunction(new Vector2f(x, y), v2, v3) / area;
        float w2 = edgeFunction(v1, new Vector2f(x, y), v3) / area;
        float w3 = edgeFunction(v1, v2, new Vector2f(x, y)) / area;

        return w1 * t1 + w2 * t2 + w3 * t3;
    }

    private static Color getTextureColor(Image texture, float u, float v) {
        if (texture == null) return Color.WHITE;

        int x = (int) (u * (texture.getWidth() - 1));
        int y = (int) (v * (texture.getHeight() - 1));

        return texture.getPixelReader().getColor(x, y);
    }

    private static Color calculateLighting(Vector3f normal, Vector3f lightPosition, Color baseColor) {
        Vector3f lightDirection = Vector3f.deduct(lightPosition, normal).normalize();
        float dot = Math.max(0, normal.dot(lightDirection));
        return new Color(
                baseColor.getRed() * dot,
                baseColor.getGreen() * dot,
                baseColor.getBlue() * dot,
                baseColor.getOpacity()
        );
    }

    private static boolean isPointInTriangle(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3) {
        float d1 = edgeFunction(new Vector2f(x, y), v1, v2);
        float d2 = edgeFunction(new Vector2f(x, y), v2, v3);
        float d3 = edgeFunction(new Vector2f(x, y), v3, v1);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private static float interpolateDepth(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3, Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d) {
        float area = edgeFunction(v1, v2, v3);
        float w1 = edgeFunction(new Vector2f(x, y), v2, v3) / area;
        float w2 = edgeFunction(v1, new Vector2f(x, y), v3) / area;
        float w3 = edgeFunction(v1, v2, new Vector2f(x, y)) / area;

        return w1 * v1_3d.getZ() + w2 * v2_3d.getZ() + w3 * v3_3d.getZ();
    }

    private static Vector3f interpolateNormal(int x, int y, Vector2f v1, Vector2f v2, Vector2f v3, Vector3f n1, Vector3f n2, Vector3f n3) {
        float area = edgeFunction(v1, v2, v3);
        float w1 = edgeFunction(new Vector2f(x, y), v2, v3) / area;
        float w2 = edgeFunction(v1, new Vector2f(x, y), v3) / area;
        float w3 = edgeFunction(v1, v2, new Vector2f(x, y)) / area;

        return new Vector3f(
                w1 * n1.getX() + w2 * n2.getX() + w3 * n3.getX(),
                w1 * n1.getY() + w2 * n2.getY() + w3 * n3.getY(),
                w1 * n1.getZ() + w2 * n2.getZ() + w3 * n3.getZ()
        );
    }
}