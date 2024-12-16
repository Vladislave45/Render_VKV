package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;

public class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<>();
    public ArrayList<Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();

    private Vector3f scale = new Vector3f(1, 1, 1);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private Vector3f translation = new Vector3f(0, 0, 0);

    private String name = "Unnamed Model";

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}