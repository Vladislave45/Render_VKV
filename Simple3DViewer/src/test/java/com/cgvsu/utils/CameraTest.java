package com.cgvsu.utils;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.render_engine.Camera;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    @Test
    void testCameraInitialization() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Camera camera = new Camera(position, target, 60, 1.0f, 0.1f, 100.0f);

        assertEquals(position, camera.getPosition());
        assertEquals(target, camera.getTarget());
    }

    @Test
    void testMovePositionAndTarget() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Camera camera = new Camera(position, target, 60, 1.0f, 0.1f, 100.0f);

        Vector3f translation = new Vector3f(1, 1, 1);
        camera.movePositionAndTarget(translation);

        assertEquals(new Vector3f(1, 1, 6), camera.getPosition());
        assertEquals(new Vector3f(1, 1, 1), camera.getTarget());
    }

    @Test
    void testSetPositionAndTarget() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Camera camera = new Camera(position, target, 60, 1.0f, 0.1f, 100.0f);

        Vector3f newPosition = new Vector3f(10, 10, 10);
        Vector3f newTarget = new Vector3f(5, 5, 5);
        camera.setPosition(newPosition);
        camera.setTarget(newTarget);

        assertEquals(newPosition, camera.getPosition());
        assertEquals(newTarget, camera.getTarget());
    }

    @Test
    void testRotateAroundTarget() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Camera camera = new Camera(position, target, 60, 1.0f, 0.1f, 100.0f);

        camera.rotateAroundTarget(90, 0); // Поворот на 90 градусов по оси Y

        // Проверяем, что позиция камеры изменилась
        assertNotEquals(position, camera.getPosition());
        assertEquals(target, camera.getTarget());
    }

    @Test
    void testGetViewMatrix() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Camera camera = new Camera(position, target, 60, 1.0f, 0.1f, 100.0f);

        Matrix4f viewMatrix = camera.getViewMatrix();
        assertNotNull(viewMatrix);
        // Проверка конкретных значений матрицы (например, первых элементов)
    }

    @Test
    void testGetProjectionMatrix() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Camera camera = new Camera(position, target, 60, 1.0f, 0.1f, 100.0f);

        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        assertNotNull(projectionMatrix);
        // Проверка конкретных значений матрицы (например, первых элементов)
    }
}
