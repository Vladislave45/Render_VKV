package com.cgvsu.render_engine;

import java.util.ArrayList;
import java.util.List;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Polygon;
import com.cgvsu.utils.TriangleRasterization;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.model.Model;
import com.cgvsu.math.Vector3f;
import com.cgvsu.utils.ZBuffer;
import com.cgvsu.math.matrix.Matrix4f;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final List<Integer> selectedVertices,
            final Color modelColor,
            final Color backgroundColor,
            final boolean isRasterizationEnabled,
            final Image texture,
            final Color fillColor
    ) {
        graphicsContext.setStroke(modelColor);
        graphicsContext.setFill(backgroundColor);
        graphicsContext.fillRect(0, 0, width, height);

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

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                Vector4f vertexVecmath = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1);

                Point2f resultPoint = GraphicConveyor.vertexToPoint(Matrix4f.multiply(modelViewProjectionMatrix, vertexVecmath).normalizeTo3f(), width, height);
                resultPoints.add(resultPoint);
            }

            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y);
            }

            if (nVerticesInPolygon > 0)
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y);
        }

        if (!selectedVertices.isEmpty()) {
            highlightSelectedVertices(graphicsContext, mesh, modelViewProjectionMatrix, width, height, selectedVertices);
        }
    }

    // Метод для выделения вершин цветом
    private static void highlightSelectedVertices(
            GraphicsContext gc,
            Model mesh,
            Matrix4f modelViewProjectionMatrix,
            int width,
            int height,
            List<Integer> selectedVertices
    ) {

        gc.setLineWidth(1);

        for (int vertexIndex : selectedVertices) {
            Vector3f vertex = mesh.vertices.get(vertexIndex);
            Vector4f vertexVecmath = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1);
            Point2f screenPoint = vertexToPoint(Matrix4f.multiply(modelViewProjectionMatrix, vertexVecmath).normalizeTo3f(), width, height);

            // Рисуем круг вокруг выделенной вершины
            gc.strokeOval(screenPoint.x - 5, screenPoint.y - 5, 10, 10);
        }
    }

    // Добавленные методы из второго кода

    private static Matrix4f modelViewProjectionMatrix;

    public static void renderWithRasterization(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final boolean isRasterizationEnabled,
            final Image texture,
            final Color fillColor
    ) {
        // Создаем Z-буфер
        List<List<Float>> zBuffer = ZBuffer.createZBuffer(width, height);

        // Создаем список нормалей для вершин
        ArrayList<Vector3f> vertexNormals = new ArrayList<>();
        for (int i = 0; i < mesh.vertices.size(); i++) {
            vertexNormals.add(new Vector3f(0, 0, 0));
        }

        // Вычисляем матрицу ModelViewProjection
        if (modelViewProjectionMatrix == null) {
            Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate(
                    mesh.getScale().getX(), mesh.getScale().getY(), mesh.getScale().getZ(),
                    mesh.getRotation().getX(), mesh.getRotation().getY(), mesh.getRotation().getZ(),
                    mesh.getTranslation().getX(), mesh.getTranslation().getY(), mesh.getTranslation().getZ()
            );
            Matrix4f viewMatrix = camera.getViewMatrix();
            Matrix4f projectionMatrix = camera.getProjectionMatrix();

            modelViewProjectionMatrix = Matrix4f.multiply(projectionMatrix, Matrix4f.multiply(viewMatrix, modelMatrix));
        }

        // Вычисляем нормали для каждой вершины
        for (Polygon polygon : mesh.polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            Vector3f v1 = mesh.vertices.get(vertexIndices.get(0));
            Vector3f v2 = mesh.vertices.get(vertexIndices.get(1));
            Vector3f v3 = mesh.vertices.get(vertexIndices.get(2));

            Vector3f normal = Vector3f.crossProduct(Vector3f.deduct(v2, v1), Vector3f.deduct(v3, v1)).normalize();

            for (int index : vertexIndices) {
                vertexNormals.get(index).add(normal);
            }
        }

        // Нормализуем нормали
        for (Vector3f normal : vertexNormals) {
            normal.normalize();
        }

        // Рендерим полигоны
        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Vector2f> resultPoints = new ArrayList<>();
            ArrayList<Vector3f> vertices3D = new ArrayList<>();
            ArrayList<Vector2f> textureCoords = new ArrayList<>();

            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                vertices3D.add(vertex);

                Vector4f vertexVecmath = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1);
                Vector3f transformedVertex = Matrix4f.multiply(modelViewProjectionMatrix, vertexVecmath).normalizeTo3f();

                Vector2f resultPoint = GraphicConveyor.vertexToVector2f(transformedVertex, width, height);
                resultPoints.add(resultPoint);

                Vector2f texCoord = mesh.textureVertices.get(mesh.polygons.get(polygonInd).getTextureVertexIndices().get(vertexInPolygonInd));
                textureCoords.add(texCoord);
            }

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

                    rasterizeTriangle(
                            graphicsContext,
                            v1, v2, v3,
                            v1_3d, v2_3d, v3_3d,
                            zBuffer,
                            width, height,
                            texture,
                            t1, t2, t3,
                            vertexNormals,
                            camera,
                            fillColor
                    );
                }
            } else {
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
            List<List<Float>> zBuffer,
            int width, int height,
            Image texture,
            Vector2f t1, Vector2f t2, Vector2f t3,
            ArrayList<Vector3f> vertexNormals,
            Camera camera,
            Color fillColor
    ) {
        ArrayList<Vector2f> triangle2D = new ArrayList<>();
        triangle2D.add(v1);
        triangle2D.add(v2);
        triangle2D.add(v3);

        TriangleRasterization.drawTriangle(gc, triangle2D, texture, t1, t2, t3, zBuffer, v1_3d, v2_3d, v3_3d, vertexNormals, camera, fillColor);
    }
}