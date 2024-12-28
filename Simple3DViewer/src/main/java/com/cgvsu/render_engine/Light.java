package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;

public class Light {
    private Vector3f position;
    private Color color;

    public Light(Vector3f position, Color color) {
        this.position = position;
        this.color = color;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Light at (" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")";
    }
}
