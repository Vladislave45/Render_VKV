package com.cgvsu.utils;

import java.util.ArrayList;

public class ZBuffer {

    public static ArrayList<ArrayList<Float>> createZBuffer(int width, int height) {
        ArrayList<ArrayList<Float>> zBuffer = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            ArrayList<Float> column = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                column.add(Float.MAX_VALUE); // Изначально глубина максимальна
            }
            zBuffer.add(column);
        }
        return zBuffer;
    }

    public static boolean testBuffer(int x, int y, float depth, ArrayList<ArrayList<Float>> zBuffer) {
        if (x >= 0 && x < zBuffer.size() && y >= 0 && y < zBuffer.get(0).size()) {
            if (depth < zBuffer.get(x).get(y)) {
                zBuffer.get(x).set(y, depth); // Обновляем глубину, если текущая меньше
                return true;
            }
        }
        return false;
    }
}