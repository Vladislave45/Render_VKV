package com.cgvsu.math.matrix;

import com.cgvsu.math.VectorAndMatrix;

public interface Matrix extends VectorAndMatrix {
    @Override
    Matrix add(VectorAndMatrix other);
    @Override
    Matrix deduct(VectorAndMatrix other);
    @Override
    Matrix multiply(float scalar);
    @Override
    Matrix divide(float scalar);
    @Override
    Matrix transpose();
    @Override
    float determinate();
}