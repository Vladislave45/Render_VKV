package com.cgvsu.math.matrix;

import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Matrix4fTest {

    @Test
    public void testConstructor() {
        float[][] data = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4f matrix = new Matrix4f(data);
        Assertions.assertArrayEquals(data, matrix.getMatrix());
    }

    @Test
    public void testDefaultConstructor() {
        Matrix4f matrix = new Matrix4f();
        float[][] expected = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        Assertions.assertArrayEquals(expected, matrix.getMatrix());
    }

    @Test
    public void testAdd() {
        Matrix4f matrix1 = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f matrix2 = new Matrix4f(new float[][]{
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        });
        Matrix4f expected = new Matrix4f(new float[][]{
                {17, 17, 17, 17},
                {17, 17, 17, 17},
                {17, 17, 17, 17},
                {17, 17, 17, 17}
        });
        Assertions.assertTrue(expected.equals(matrix1.add(matrix2)));
    }

    @Test
    public void testDeduct() {
        Matrix4f matrix1 = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f matrix2 = new Matrix4f(new float[][]{
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        });
        Matrix4f expected = new Matrix4f(new float[][]{
                {-15, -13, -11, -9},
                {-7, -5, -3, -1},
                {1, 3, 5, 7},
                {9, 11, 13, 15}
        });
        Assertions.assertTrue(expected.equals(matrix1.deduct(matrix2)));
    }

    @Test
    public void testMultiplyMatrix() {
        Matrix4f matrix1 = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f matrix2 = new Matrix4f(new float[][]{
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        });
        Matrix4f expected = new Matrix4f(new float[][]{
                {80, 70, 60, 50},
                {240, 214, 188, 162},
                {400, 358, 316, 274},
                {560, 502, 444, 386}
        });
        Assertions.assertTrue(expected.equals(matrix1.multiply(matrix2)));
    }

    @Test
    public void testMultiplyVector() {
        Matrix4f matrix = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Vector4f vector = new Vector4f(1, 2, 3, 4);
        Vector4f expected = new Vector4f(30, 70, 110, 150);
        Assertions.assertTrue(expected.equals(matrix.multiply(vector)));
    }

    @Test
    public void testTranspose() {
        Matrix4f matrix = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f expected = new Matrix4f(new float[][]{
                {1, 5, 9, 13},
                {2, 6, 10, 14},
                {3, 7, 11, 15},
                {4, 8, 12, 16}
        });
        Assertions.assertTrue(expected.equals(matrix.transpose()));
    }

    @Test
    public void testUnit() {
        Matrix4f expected = new Matrix4f(new float[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
        Assertions.assertTrue(expected.equals(Matrix4f.unit()));
    }

    @Test
    public void testDeterminate() {
        Matrix4f matrix = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Assertions.assertEquals(0, matrix.determinate());
    }

    @Test
    public void testMinor() {
        Matrix4f matrix = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f expected = new Matrix4f(new float[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
        Assertions.assertTrue(expected.equals(matrix.minor()));
    }

    @Test
    public void testEquals() {
        Matrix4f matrix1 = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f matrix2 = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Assertions.assertTrue(matrix1.equals(matrix2));
    }

    @Test
    public void testNotEquals() {
        Matrix4f matrix1 = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f matrix2 = new Matrix4f(new float[][]{
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        });
        Assertions.assertFalse(matrix1.equals(matrix2));
    }
}