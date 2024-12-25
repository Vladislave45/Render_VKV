package com.cgvsu.utils;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NormalUtilsTest {

    @Test
    void testRecalculateVertexNormals() {
        Model model = new Model();
        model.vertices = (ArrayList<Vector3f>) Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        );

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(Arrays.asList(0, 1, 2));
        model.polygons = (ArrayList<Polygon>) Arrays.asList(polygon);

        List<Vector3f> normals = NormalUtils.recalculateVertexNormals(model);

        assertEquals(3, normals.size());
        assertEquals(new Vector3f(0, 0, 1), normals.get(0));
        assertEquals(new Vector3f(0, 0, 1), normals.get(1));
        assertEquals(new Vector3f(0, 0, 1), normals.get(2));
    }

    @Test
    void testComputePolygonNormal() {
        List<Vector3f> vertices = Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        );

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(Arrays.asList(0, 1, 2));

        Vector3f normal = NormalUtils.computePolygonNormal(polygon, vertices);

        assertEquals(new Vector3f(0, 0, 1), normal);
    }

    @Test
    void testRecalculateVertexNormalsWithMultiplePolygons() {
        Model model = new Model();
        model.vertices = (ArrayList<Vector3f>) Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(1, 1, 0)
        );

        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(Arrays.asList(0, 1, 2));
        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(Arrays.asList(1, 2, 3));
        model.polygons = (ArrayList<Polygon>) Arrays.asList(polygon1, polygon2);

        List<Vector3f> normals = NormalUtils.recalculateVertexNormals(model);

        assertEquals(4, normals.size());
        assertEquals(new Vector3f(0, 0, 1), normals.get(0));
        assertEquals(new Vector3f(0, 0, 1), normals.get(1));
        assertEquals(new Vector3f(0, 0, 1), normals.get(2));
        assertEquals(new Vector3f(0, 0, 1), normals.get(3));
    }
}
