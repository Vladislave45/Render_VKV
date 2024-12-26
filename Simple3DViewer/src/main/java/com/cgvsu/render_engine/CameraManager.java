package com.cgvsu.render_engine;

import java.util.ArrayList;
import java.util.List;

public class CameraManager {
    private List<Camera> cameras;
    private int activeCameraIndex;

    public CameraManager() {
        cameras = new ArrayList<>();
        activeCameraIndex = -1;
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);
        if (activeCameraIndex == -1) {
            activeCameraIndex = 0;
        }
    }

    public void removeCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            cameras.remove(index);
            if (cameras.isEmpty()) {
                activeCameraIndex = -1;
            } else if (index <= activeCameraIndex) {
                activeCameraIndex = Math.max(0, activeCameraIndex - 1);
            }
        }
    }

    public void setActiveCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            activeCameraIndex = index;
        }
    }

    public Camera getActiveCamera() {
        if (activeCameraIndex >= 0 && activeCameraIndex < cameras.size()) {
            return cameras.get(activeCameraIndex);
        }
        return null;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public int getActiveCameraIndex() {
        return activeCameraIndex;
    }
}