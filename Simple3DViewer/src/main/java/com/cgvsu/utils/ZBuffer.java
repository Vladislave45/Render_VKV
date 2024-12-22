package com.cgvsu.utils;

import java.util.ArrayList;
import java.util.List;

public class ZBuffer {

    public static List<List<Float>> createZBuffer(int width, int height) {
        List<List<Float>> zBuffer = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            List<Float> column = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                column.add(Float.MAX_VALUE); // Инициализация максимальным значением
            }
            zBuffer.add(column);
        }
        return zBuffer;
    }

    public static boolean testBuffer(int x, int y, float depth, List<List<Float>> zBuffer) {
        if (x >= 0 && x < zBuffer.size() && y >= 0 && y < zBuffer.get(0).size()) {
            if (depth < zBuffer.get(x).get(y)) {
                zBuffer.get(x).set(y, depth);
                return true;
            }
        }
        return false;
    }
}