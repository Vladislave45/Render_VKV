package com.cgvsu.render_engine;

import java.util.ArrayList;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.model.Model;
import javafx.scene.canvas.GraphicsContext;
import javax.vecmath.Point2f;
import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = modelMatrix;
        modelViewProjectionMatrix = modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix = modelViewProjectionMatrix.multiply(projectionMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                // Преобразование Vector3f в Vector4f для работы с гомогенными координатами
                Vector4f vertex4f = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0f);
                Vector4f transformedVertex = modelViewProjectionMatrix.multiply(vertex4f);

                // Преобразование обратно в Vector3f для дальнейшей обработки
                Vector3f transformedVertex3f = new Vector3f(transformedVertex.getX(), transformedVertex.getY(), transformedVertex.getZ());
                Vector2f vector2f = Vector3f.vertex3fToVector2f(transformedVertex3f, width, height);
                Point2f resultPoint = new Point2f(vector2f.getX(), vector2f.getY());
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
}