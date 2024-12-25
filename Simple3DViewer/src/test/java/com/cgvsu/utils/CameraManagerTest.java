package com.cgvsu.utils;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.CameraManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraManagerTest {

    @Test
    void testAddCamera() {
        CameraManager cameraManager = new CameraManager();
        Camera camera = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 60, 1.0f, 0.1f, 100.0f);

        cameraManager.addCamera(camera);

        assertEquals(1, cameraManager.getCameras().size());
        assertEquals(camera, cameraManager.getActiveCamera());
    }

    @Test
    void testRemoveCamera() {
        CameraManager cameraManager = new CameraManager();
        Camera camera1 = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 60, 1.0f, 0.1f, 100.0f);
        Camera camera2 = new Camera(new Vector3f(10, 10, 10), new Vector3f(5, 5, 5), 60, 1.0f, 0.1f, 100.0f);

        cameraManager.addCamera(camera1);
        cameraManager.addCamera(camera2);

        cameraManager.removeCamera(0);

        assertEquals(1, cameraManager.getCameras().size());
        assertEquals(camera2, cameraManager.getActiveCamera());
    }

    @Test
    void testSetActiveCamera() {
        CameraManager cameraManager = new CameraManager();
        Camera camera1 = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 60, 1.0f, 0.1f, 100.0f);
        Camera camera2 = new Camera(new Vector3f(10, 10, 10), new Vector3f(5, 5, 5), 60, 1.0f, 0.1f, 100.0f);

        cameraManager.addCamera(camera1);
        cameraManager.addCamera(camera2);

        cameraManager.setActiveCamera(1);

        assertEquals(camera2, cameraManager.getActiveCamera());
    }

    @Test
    void testGetActiveCameraIndex() {
        CameraManager cameraManager = new CameraManager();
        Camera camera1 = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 60, 1.0f, 0.1f, 100.0f);
        Camera camera2 = new Camera(new Vector3f(10, 10, 10), new Vector3f(5, 5, 5), 60, 1.0f, 0.1f, 100.0f);

        cameraManager.addCamera(camera1);
        cameraManager.addCamera(camera2);

        cameraManager.setActiveCamera(1);

        assertEquals(1, cameraManager.getActiveCameraIndex());
    }

    @Test
    void testRemoveLastCamera() {
        CameraManager cameraManager = new CameraManager();
        Camera camera = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 60, 1.0f, 0.1f, 100.0f);

        cameraManager.addCamera(camera);
        cameraManager.removeCamera(0);

        assertNull(cameraManager.getActiveCamera());
        assertEquals(-1, cameraManager.getActiveCameraIndex());
    }

    @Test
    void testRemoveInvalidCamera() {
        CameraManager cameraManager = new CameraManager();
        Camera camera = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 60, 1.0f, 0.1f, 100.0f);

        cameraManager.addCamera(camera);

        assertThrows(IndexOutOfBoundsException.class, () -> cameraManager.removeCamera(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> cameraManager.removeCamera(1));
    }

    @Test
    void testSetInvalidActiveCamera() {
        CameraManager cameraManager = new CameraManager();
        Camera camera = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 60, 1.0f, 0.1f, 100.0f);

        cameraManager.addCamera(camera);

        assertThrows(IndexOutOfBoundsException.class, () -> cameraManager.setActiveCamera(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> cameraManager.setActiveCamera(1));
    }
}
