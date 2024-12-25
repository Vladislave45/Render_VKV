package com.cgvsu.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZBufferTest {

    @Test
    void testCreateZBuffer() {
        List<List<Float>> zBuffer = ZBuffer.createZBuffer(2, 2);

        assertEquals(2, zBuffer.size());
        assertEquals(2, zBuffer.get(0).size());
        assertEquals(Float.MAX_VALUE, zBuffer.get(0).get(0));
    }

    @Test
    void testTestBuffer() {
        List<List<Float>> zBuffer = ZBuffer.createZBuffer(2, 2);

        assertTrue(ZBuffer.testBuffer(0, 0, 1.0f, zBuffer));
        assertFalse(ZBuffer.testBuffer(0, 0, 2.0f, zBuffer));
        assertTrue(ZBuffer.testBuffer(0, 0, 0.5f, zBuffer));
    }

    @Test
    void testTestBufferWithInvalidCoordinates() {
        List<List<Float>> zBuffer = ZBuffer.createZBuffer(2, 2);

        assertFalse(ZBuffer.testBuffer(-1, -1, 1.0f, zBuffer));
        assertFalse(ZBuffer.testBuffer(2, 2, 1.0f, zBuffer));
    }
}
