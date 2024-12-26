package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.utils.NormalUtils;
import com.cgvsu.utils.TriangleRasterization;
import com.cgvsu.utils.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;

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
            Color fillColor,
            final boolean isWireframeEnabled,
            final boolean useLighting,
            final Vector3f lightPosition,
            final Color lightColor
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

        if (isRasterizationEnabled) {
            renderWithRasterization(graphicsContext, camera, mesh, width, height, modelViewProjectionMatrix, texture, fillColor, useLighting, lightPosition, lightColor);
        }

        if (isWireframeEnabled) {
            renderWithoutRasterization(graphicsContext, mesh, width, height, modelViewProjectionMatrix, selectedVertices);
        }

        if (!selectedVertices.isEmpty()) {
            highlightSelectedVertices(graphicsContext, mesh, modelViewProjectionMatrix, width, height, selectedVertices);
        }
    }

    private static void renderWithRasterization(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            int width, int height,
            Matrix4f modelViewProjectionMatrix,
            Image texture,
            Color fillColor,
            boolean useLighting,
            Vector3f lightPosition,
            Color lightColor
    ) {
        List<List<Float>> zBuffer = ZBuffer.createZBuffer(width, height);
        List<Vector3f> vertexNormals = NormalUtils.recalculateVertexNormals(mesh);

        for (Polygon polygon : mesh.polygons) {
            List<Vector2f> resultPoints = new ArrayList<>();
            List<Vector3f> vertices3D = new ArrayList<>();
            List<Vector2f> textureCoords = new ArrayList<>();

            for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
                Vector3f vertex = mesh.vertices.get(polygon.getVertexIndices().get(i));
                vertices3D.add(vertex);

                Vector4f vertexVecmath = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1);
                Vector3f transformedVertex = Matrix4f.multiply(modelViewProjectionMatrix, vertexVecmath).normalizeTo3f();

                Vector2f resultPoint = GraphicConveyor.vertexToVector2f(transformedVertex, width, height);
                resultPoints.add(resultPoint);

                Vector2f texCoord = mesh.textureVertices.get(polygon.getTextureVertexIndices().get(i));
                textureCoords.add(texCoord);
            }

            for (int i = 0; i < resultPoints.size() - 2; i++) {
                Vector2f v1 = resultPoints.get(0);
                Vector2f v2 = resultPoints.get(i + 1);
                Vector2f v3 = resultPoints.get(i + 2);

                Vector3f v1_3d = vertices3D.get(0);
                Vector3f v2_3d = vertices3D.get(i + 1);
                Vector3f v3_3d = vertices3D.get(i + 2);

                Vector2f t1 = textureCoords.get(0);
                Vector2f t2 = textureCoords.get(i + 1);
                Vector2f t3 = textureCoords.get(i + 2);

                TriangleRasterization.drawTriangle(
                        gc,
                        List.of(v1, v2, v3),
                        texture,
                        t1, t2, t3,
                        zBuffer,
                        v1_3d, v2_3d, v3_3d,
                        List.of(vertexNormals.get(polygon.getVertexIndices().get(0)),
                                vertexNormals.get(polygon.getVertexIndices().get(i + 1)),
                                vertexNormals.get(polygon.getVertexIndices().get(i + 2))),
                        camera,
                        fillColor,
                        width,
                        height,
                        useLighting,
                        lightPosition,
                        lightColor
                );
            }
        }
    }

    private static void renderWithoutRasterization(
            GraphicsContext graphicsContext,
            Model mesh,
            int width, int height,
            Matrix4f modelViewProjectionMatrix,
            List<Integer> selectedVertices
    ) {
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
    }

    private static void highlightSelectedVertices(
            GraphicsContext gc,
            Model mesh,
            Matrix4f modelViewProjectionMatrix,
            int width, int height,
            List<Integer> selectedVertices
    ) {
        gc.setLineWidth(1);

        for (int vertexIndex : selectedVertices) {
            Vector3f vertex = mesh.vertices.get(vertexIndex);
            Vector4f vertexVecmath = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1);
            Point2f screenPoint = vertexToPoint(Matrix4f.multiply(modelViewProjectionMatrix, vertexVecmath).normalizeTo3f(), width, height);

            gc.strokeOval(screenPoint.x - 5, screenPoint.y - 5, 10, 10);
        }
    }
}