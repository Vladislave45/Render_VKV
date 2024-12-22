package com.cgvsu.math;

public interface VectorAndMatrix {
    VectorAndMatrix add(VectorAndMatrix other);
    VectorAndMatrix deduct(VectorAndMatrix other);
    VectorAndMatrix multiply(float scalar);
    VectorAndMatrix divide(float scalar);
    default VectorAndMatrix transpose() {
        throw new UnsupportedOperationException("Error");
    }
    default float length() {
        throw new UnsupportedOperationException("Error");
    }
    default float determinate() {
        throw new UnsupportedOperationException("Error");
    }
    default VectorAndMatrix normalize() {
        throw new UnsupportedOperationException("Error");
    }
    default float dot(VectorAndMatrix other) {
        throw new UnsupportedOperationException("Error");
    }
}