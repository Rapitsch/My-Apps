package com.example

import com.example.geometry.ArchitectureMeshes
import com.example.geometry.Vec3
import com.example.model.ArchitecturalPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAllArchitecturalPresetsGenerateValidGeometry() {
        ArchitecturalPreset.values().forEach { preset ->
            val model = ArchitectureMeshes.generateModel(preset)
            assertNotNull(model)
            assertTrue("Vertices must not be empty for $preset", model.vertices.isNotEmpty())
            assertTrue("Edges must not be empty for $preset", model.edges.isNotEmpty())
            assertTrue("Faces must not be empty for $preset", model.faces.isNotEmpty())
        }
    }

    @Test
    fun testVec3MathAndRotation() {
        val v = Vec3(1f, 0f, 0f)
        val rotated = v.rotateY(Math.PI.toFloat() / 2f)
        assertEquals(0f, rotated.x, 0.001f)
        assertEquals(0f, rotated.y, 0.001f)
        assertEquals(-1f, rotated.z, 0.001f)

        val v1 = Vec3(1f, 2f, 3f)
        val v2 = Vec3(4f, 5f, 6f)
        val sum = v1 + v2
        assertEquals(5f, sum.x, 0.001f)
        assertEquals(7f, sum.y, 0.001f)
        assertEquals(9f, sum.z, 0.001f)
    }
}
