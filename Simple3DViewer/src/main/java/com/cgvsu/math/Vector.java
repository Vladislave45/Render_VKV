package com.cgvsu.math;

public interface Vector extends VectorAndMatrix {
    @Override
    Vector add(VectorAndMatrix other);
    @Override
    Vector deduct(VectorAndMatrix other);
    @Override
    Vector multiply(float scalar);
    @Override
    Vector divide(float scalar);
    @Override
    float length();
    @Override
    Vector normalize();
    @Override
    float dot(VectorAndMatrix other);
}