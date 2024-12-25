package com.cgvsu.utils;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriangulationTest {

    @Test
    void testGetTriangulatedModel() {
        Model model = new Model();
        model.vertices = (ArrayList<Vector3f>) Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(1, 1, 0)
        );

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(Arrays.asList(0, 1, 2, 3));
        model.polygons = (ArrayList<Polygon>) Arrays.asList(polygon);

        Model triangulatedModel = Triangulation.getTriangulatedModel(model);

        assertEquals(2, triangulatedModel.polygons.size());
        assertEquals(3, triangulatedModel.polygons.get(0).getVertexIndices().size());
        assertEquals(3, triangulatedModel.polygons.get(1).getVertexIndices().size());
    }

    @Test
    void testTriangulate() {
        List<Vector3f> vertices = Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(1, 1, 0)
        );

        List<List<Vector3f>> triangles = Triangulation.triangulate(vertices);

        assertEquals(2, triangles.size());
        assertEquals(3, triangles.get(0).size());
        assertEquals(3, triangles.get(1).size());
    }

    @Test
    void testGetTriangulatedModelWithMultiplePolygons() {
        Model model = new Model();
        model.vertices = (ArrayList<Vector3f>) Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(1, 1, 0),
                new Vector3f(0.5f, 1.5f, 0)
        );

        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(Arrays.asList(0, 1, 2, 3));
        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(Arrays.asList(1, 2, 3, 4));
        model.polygons = (ArrayList<Polygon>) Arrays.asList(polygon1, polygon2);

        Model triangulatedModel = Triangulation.getTriangulatedModel(model);

        assertEquals(4, triangulatedModel.polygons.size()); // 2 полигона * 2 треугольника
    }
}