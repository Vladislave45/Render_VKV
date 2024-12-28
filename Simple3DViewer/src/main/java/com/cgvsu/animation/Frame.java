package com.cgvsu.animation;

public class Frame {
    private float scaleX, scaleY, scaleZ;
    private float rotateX, rotateY, rotateZ;
    private float translateX, translateY, translateZ;
    private int duration; // в миллисекундах

    public Frame(float scaleX, float scaleY, float scaleZ,
                 float rotateX, float rotateY, float rotateZ,
                 float translateX, float translateY, float translateZ,
                 int duration) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
        this.translateX = translateX;
        this.translateY = translateY;
        this.translateZ = translateZ;
        this.duration = duration;
    }

    public float getScaleX() { return scaleX; }
    public float getScaleY() { return scaleY; }
    public float getScaleZ() { return scaleZ; }
    public float getRotateX() { return rotateX; }
    public float getRotateY() { return rotateY; }
    public float getRotateZ() { return rotateZ; }
    public float getTranslateX() { return translateX; }
    public float getTranslateY() { return translateY; }
    public float getTranslateZ() { return translateZ; }
    public int getDuration() { return duration; }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
