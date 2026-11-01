package com.example.geometry

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vec3 {
        val len = length()
        return if (len > 0.00001f) this / len else Vec3(0f, 1f, 0f)
    }

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vec3): Vec3 = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun rotateX(rad: Float): Vec3 {
        val c = cos(rad)
        val s = sin(rad)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(rad: Float): Vec3 {
        val c = cos(rad)
        val s = sin(rad)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(rad: Float): Vec3 {
        val c = cos(rad)
        val s = sin(rad)
        return Vec3(x * c - y * s, x * s + y * c, z)
    }

    fun rotateEuler(rotX: Float, rotY: Float, rotZ: Float): Vec3 {
        return this.rotateY(rotY).rotateX(rotX).rotateZ(rotZ)
    }
}

data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val depth: Float,
    val index: Int
)

data class Edge3D(
    val i1: Int,
    val i2: Int,
    val bandCategory: Int = 0 // 0: foundation/sub-bass, 1: mid/pillar, 2: high/spire
)

data class Face3D(
    val i1: Int,
    val i2: Int,
    val i3: Int,
    val normal: Vec3 = Vec3(0f, 0f, 1f),
    val tier: Int = 0
)

data class ArchitecturalModel(
    val vertices: List<Vec3>,
    val edges: List<Edge3D>,
    val faces: List<Face3D>,
    val baseBounds: Float = 1.0f
)
