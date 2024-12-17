package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Model;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.utils.ZBuffer;
import com.cgvsu.utils.triangles_utils.TriangleRasterization;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;


public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final boolean isRasterizationEnabled,
            final Image texture
    ) {
        // Создаём Z-буфер
        ArrayList<ArrayList<Float>> zBuffer = ZBuffer.createZBuffer(width, height);

        // Матрицы преобразования
        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate(
                mesh.getScale().getX(), mesh.getScale().getY(), mesh.getScale().getZ(),
                mesh.getRotation().getX(), mesh.getRotation().getY(), mesh.getRotation().getZ(),
                mesh.getTranslation().getX(), mesh.getTranslation().getY(), mesh.getTranslation().getZ()
        );
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = Matrix4f.multiply(projectionMatrix, Matrix4f.multiply(viewMatrix, modelMatrix));

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Vector2f> resultPoints = new ArrayList<>();
            ArrayList<Vector3f> vertices3D = new ArrayList<>();
            ArrayList<Vector2f> textureCoords = new ArrayList<>(); // Текстурные координаты

            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                vertices3D.add(vertex);

                Vector4f vertexVecmath = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1);
                Vector3f transformedVertex = Matrix4f.multiply(modelViewProjectionMatrix, vertexVecmath).normalizeTo3f();

                Vector2f resultPoint = GraphicConveyor.vertexToVector2f(transformedVertex, width, height);
                resultPoints.add(resultPoint);

                // Добавляем текстурные координаты
                Vector2f texCoord = mesh.textureVertices.get(mesh.polygons.get(polygonInd).getTextureVertexIndices().get(vertexInPolygonInd));
                textureCoords.add(texCoord);
            }

            // Растеризация треугольников
            if (isRasterizationEnabled) {
                for (int i = 0; i < nVerticesInPolygon - 2; i++) {
                    Vector2f v1 = resultPoints.get(0);
                    Vector2f v2 = resultPoints.get(i + 1);
                    Vector2f v3 = resultPoints.get(i + 2);

                    Vector3f v1_3d = vertices3D.get(0);
                    Vector3f v2_3d = vertices3D.get(i + 1);
                    Vector3f v3_3d = vertices3D.get(i + 2);

                    Vector2f t1 = textureCoords.get(0);
                    Vector2f t2 = textureCoords.get(i + 1);
                    Vector2f t3 = textureCoords.get(i + 2);

                    // Используем Z-буфер для управления видимостью
                    rasterizeTriangle(graphicsContext, v1, v2, v3, v1_3d, v2_3d, v3_3d, zBuffer, width, height, texture, t1, t2, t3);
                }
            } else {
                // Если растеризация выключена, отрисовываем только линии
                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                    graphicsContext.strokeLine(
                            resultPoints.get(vertexInPolygonInd - 1).getX(),
                            resultPoints.get(vertexInPolygonInd - 1).getY(),
                            resultPoints.get(vertexInPolygonInd).getX(),
                            resultPoints.get(vertexInPolygonInd).getY()
                    );
                }

                if (nVerticesInPolygon > 0)
                    graphicsContext.strokeLine(
                            resultPoints.get(nVerticesInPolygon - 1).getX(),
                            resultPoints.get(nVerticesInPolygon - 1).getY(),
                            resultPoints.get(0).getX(),
                            resultPoints.get(0).getY()
                    );
            }
        }
    }

    private static void rasterizeTriangle(
            GraphicsContext gc,
            Vector2f v1, Vector2f v2, Vector2f v3,
            Vector3f v1_3d, Vector3f v2_3d, Vector3f v3_3d,
            ArrayList<ArrayList<Float>> zBuffer,
            int width, int height,
            Image texture, // Добавляем текстуру
            Vector2f t1, Vector2f t2, Vector2f t3 // Текстурные координаты
    ) {
        // Преобразуем точки в 2D
        ArrayList<Vector2f> triangle2D = new ArrayList<>();
        triangle2D.add(v1);
        triangle2D.add(v2);
        triangle2D.add(v3);

        // Вызываем растеризацию треугольника
        TriangleRasterization.drawTriangle(gc, triangle2D, texture, t1, t2, t3, zBuffer, v1_3d, v2_3d, v3_3d);
    }
}