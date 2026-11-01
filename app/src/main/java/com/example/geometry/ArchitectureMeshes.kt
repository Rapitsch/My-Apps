package com.example.geometry

import com.example.model.ArchitecturalPreset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ArchitectureMeshes {

    fun generateModel(preset: ArchitecturalPreset): ArchitecturalModel {
        return when (preset) {
            ArchitecturalPreset.RESONANT_CITADEL -> createCitadelModel()
            ArchitecturalPreset.CYMATIC_TORUS -> createTorusModel()
            ArchitecturalPreset.BRUTALIST_MONOLITH -> createMonolithModel()
            ArchitecturalPreset.GEODESIC_POLYHEDRON -> createGeodesicModel()
            ArchitecturalPreset.CHLADNI_PLATE_2D -> createChladniModel()
        }
    }

    /**
     * Resonant Citadel: Multi-tiered gothic/scifi cathedral with 4 corner towers,
     * central grand spire, flying buttresses, foundation arcade, and tiered colonnades.
     */
    private fun createCitadelModel(): ArchitecturalModel {
        val verts = mutableListOf<Vec3>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        fun addQuad(v0: Int, v1: Int, v2: Int, v3: Int, tier: Int) {
            edges.add(Edge3D(v0, v1, tier))
            edges.add(Edge3D(v1, v2, tier))
            edges.add(Edge3D(v2, v3, tier))
            edges.add(Edge3D(v3, v0, tier))

            val norm = (verts[v1] - verts[v0]).cross(verts[v2] - verts[v0]).normalize()
            faces.add(Face3D(v0, v1, v2, norm, tier))
            faces.add(Face3D(v0, v2, v3, norm, tier))
        }

        // Tier 0: Foundation Base Platform (8-sided polygon)
        val baseRadius = 2.2f
        val baseSides = 8
        val baseBottomY = -1.5f
        val baseTopY = -1.1f
        val baseStartIdx = verts.size

        for (i in 0 until baseSides) {
            val angle = 2f * PI.toFloat() * i / baseSides
            verts.add(Vec3(cos(angle) * baseRadius, baseBottomY, sin(angle) * baseRadius))
        }
        for (i in 0 until baseSides) {
            val angle = 2f * PI.toFloat() * i / baseSides
            verts.add(Vec3(cos(angle) * (baseRadius * 0.9f), baseTopY, sin(angle) * (baseRadius * 0.9f)))
        }
        for (i in 0 until baseSides) {
            val next = (i + 1) % baseSides
            addQuad(baseStartIdx + i, baseStartIdx + next, baseStartIdx + baseSides + next, baseStartIdx + baseSides + i, 0)
        }

        // Tier 1: Main Colonnade & Central Keep
        val keepStartIdx = verts.size
        val keepSides = 6
        val keepRadius = 1.3f
        val keepTopY = -0.1f
        for (i in 0 until keepSides) {
            val angle = 2f * PI.toFloat() * i / keepSides
            verts.add(Vec3(cos(angle) * keepRadius, baseTopY, sin(angle) * keepRadius))
        }
        for (i in 0 until keepSides) {
            val angle = 2f * PI.toFloat() * i / keepSides
            verts.add(Vec3(cos(angle) * (keepRadius * 0.8f), keepTopY, sin(angle) * (keepRadius * 0.8f)))
        }
        for (i in 0 until keepSides) {
            val next = (i + 1) % keepSides
            addQuad(keepStartIdx + i, keepStartIdx + next, keepStartIdx + keepSides + next, keepStartIdx + keepSides + i, 1)
        }

        // Tier 2: Spire Lantern Gallery
        val lanternStartIdx = verts.size
        val lanternSides = 6
        val lanternRadius = 0.7f
        val lanternTopY = 1.0f
        for (i in 0 until lanternSides) {
            val angle = 2f * PI.toFloat() * i / lanternSides + (PI.toFloat() / 6f)
            verts.add(Vec3(cos(angle) * lanternRadius, keepTopY, sin(angle) * lanternRadius))
        }
        for (i in 0 until lanternSides) {
            val angle = 2f * PI.toFloat() * i / lanternSides + (PI.toFloat() / 6f)
            verts.add(Vec3(cos(angle) * (lanternRadius * 0.6f), lanternTopY, sin(angle) * (lanternRadius * 0.6f)))
        }
        for (i in 0 until lanternSides) {
            val next = (i + 1) % lanternSides
            addQuad(lanternStartIdx + i, lanternStartIdx + next, lanternStartIdx + lanternSides + next, lanternStartIdx + lanternSides + i, 2)
        }

        // Tier 3: Soaring Apex Needle / Pinnacle
        val apexIdx = verts.size
        verts.add(Vec3(0f, 2.3f, 0f))
        for (i in 0 until lanternSides) {
            val v1 = lanternStartIdx + lanternSides + i
            val v2 = lanternStartIdx + lanternSides + ((i + 1) % lanternSides)
            edges.add(Edge3D(v1, apexIdx, 2))
            val norm = (verts[v2] - verts[v1]).cross(verts[apexIdx] - verts[v1]).normalize()
            faces.add(Face3D(v1, v2, apexIdx, norm, 2))
        }

        // 4 Peripheral Spires (Cardinal flying towers)
        val cornerOffsets = listOf(
            Vec3(1.6f, -1.1f, 1.6f),
            Vec3(-1.6f, -1.1f, 1.6f),
            Vec3(-1.6f, -1.1f, -1.6f),
            Vec3(1.6f, -1.1f, -1.6f)
        )

        for ((towerIdx, offset) in cornerOffsets.withIndex()) {
            val tBase = verts.size
            val towerRadius = 0.28f
            val towerH = 1.5f
            for (i in 0 until 4) {
                val a = (2f * PI.toFloat() * i / 4) + (PI.toFloat() / 4)
                verts.add(Vec3(offset.x + cos(a) * towerRadius, offset.y, offset.z + sin(a) * towerRadius))
            }
            for (i in 0 until 4) {
                val a = (2f * PI.toFloat() * i / 4) + (PI.toFloat() / 4)
                verts.add(Vec3(offset.x + cos(a) * (towerRadius * 0.7f), offset.y + towerH, offset.z + sin(a) * (towerRadius * 0.7f)))
            }
            val tPeak = verts.size
            verts.add(Vec3(offset.x, offset.y + towerH + 0.6f, offset.z))

            for (i in 0 until 4) {
                val next = (i + 1) % 4
                addQuad(tBase + i, tBase + next, tBase + 4 + next, tBase + 4 + i, 1)
                edges.add(Edge3D(tBase + 4 + i, tPeak, 2))
                faces.add(Face3D(tBase + 4 + i, tBase + 4 + next, tPeak, Vec3(0f, 1f, 0f), 2))
            }

            // Flying Buttress arch connecting corner tower to central lantern
            val buttressLanternTarget = lanternStartIdx + (towerIdx % lanternSides)
            edges.add(Edge3D(tBase + 4, buttressLanternTarget, 1))
        }

        return ArchitecturalModel(verts, edges, faces, 2.5f)
    }

    /**
     * Cymatic Torus: A sacred geometry architectural torus with revolving concentric
     * ring ribs, meridians, and chiral harmonic lattice arches.
     */
    private fun createTorusModel(): ArchitecturalModel {
        val verts = mutableListOf<Vec3>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        val majorR = 1.6f
        val minorR = 0.65f
        val uSegments = 16
        val vSegments = 10

        for (u in 0 until uSegments) {
            val theta = 2f * PI.toFloat() * u / uSegments
            val cosTheta = cos(theta)
            val sinTheta = sin(theta)

            for (v in 0 until vSegments) {
                val phi = 2f * PI.toFloat() * v / vSegments
                val cosPhi = cos(phi)
                val sinPhi = sin(phi)

                val x = (majorR + minorR * cosPhi) * cosTheta
                val y = minorR * sinPhi
                val z = (majorR + minorR * cosPhi) * sinTheta
                verts.add(Vec3(x, y, z))
            }
        }

        for (u in 0 until uSegments) {
            val nextU = (u + 1) % uSegments
            for (v in 0 until vSegments) {
                val nextV = (v + 1) % vSegments

                val i0 = u * vSegments + v
                val i1 = nextU * vSegments + v
                val i2 = nextU * vSegments + nextV
                val i3 = u * vSegments + nextV

                edges.add(Edge3D(i0, i1, 1))
                edges.add(Edge3D(i0, i3, 0))
                // Diagonal acoustic lattice rib
                if ((u + v) % 2 == 0) {
                    edges.add(Edge3D(i0, i2, 2))
                }

                val norm = (verts[i1] - verts[i0]).cross(verts[i2] - verts[i0]).normalize()
                faces.add(Face3D(i0, i1, i2, norm, if (v < vSegments / 2) 0 else 1))
                faces.add(Face3D(i0, i2, i3, norm, if (v < vSegments / 2) 0 else 1))
            }
        }

        return ArchitecturalModel(verts, edges, faces, 2.3f)
    }

    /**
     * Brutalist Monolith: Stepped cantilevered cubic plinths, vertical acoustic slits,
     * deep resonant core, and floating cantilever balconies.
     */
    private fun createMonolithModel(): ArchitecturalModel {
        val verts = mutableListOf<Vec3>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        fun addBox(xMin: Float, xMax: Float, yMin: Float, yMax: Float, zMin: Float, zMax: Float, tier: Int) {
            val start = verts.size
            verts.add(Vec3(xMin, yMin, zMin)) // 0
            verts.add(Vec3(xMax, yMin, zMin)) // 1
            verts.add(Vec3(xMax, yMin, zMax)) // 2
            verts.add(Vec3(xMin, yMin, zMin)) // 3
            verts.add(Vec3(xMin, yMax, zMin)) // 4
            verts.add(Vec3(xMax, yMax, zMin)) // 5
            verts.add(Vec3(xMax, yMax, zMax)) // 6
            verts.add(Vec3(xMin, yMax, zMax)) // 7

            // 12 edges
            edges.add(Edge3D(start + 0, start + 1, tier))
            edges.add(Edge3D(start + 1, start + 2, tier))
            edges.add(Edge3D(start + 2, start + 3, tier))
            edges.add(Edge3D(start + 3, start + 0, tier))

            edges.add(Edge3D(start + 4, start + 5, tier))
            edges.add(Edge3D(start + 5, start + 6, tier))
            edges.add(Edge3D(start + 6, start + 7, tier))
            edges.add(Edge3D(start + 7, start + 4, tier))

            edges.add(Edge3D(start + 0, start + 4, tier))
            edges.add(Edge3D(start + 1, start + 5, tier))
            edges.add(Edge3D(start + 2, start + 6, tier))
            edges.add(Edge3D(start + 3, start + 7, tier))

            // Front face
            faces.add(Face3D(start + 0, start + 1, start + 5, Vec3(0f, 0f, -1f), tier))
            faces.add(Face3D(start + 0, start + 5, start + 4, Vec3(0f, 0f, -1f), tier))
            // Back face
            faces.add(Face3D(start + 3, start + 7, start + 6, Vec3(0f, 0f, 1f), tier))
            faces.add(Face3D(start + 3, start + 6, start + 2, Vec3(0f, 0f, 1f), tier))
            // Top face
            faces.add(Face3D(start + 4, start + 5, start + 6, Vec3(0f, 1f, 0f), tier))
            faces.add(Face3D(start + 4, start + 6, start + 7, Vec3(0f, 1f, 0f), tier))
        }

        // Stepped base plinths
        addBox(-1.8f, 1.8f, -1.8f, -1.4f, -1.8f, 1.8f, 0)
        addBox(-1.4f, 1.4f, -1.4f, -1.0f, -1.4f, 1.4f, 0)

        // Core Monolith Body (Tall, dense)
        addBox(-0.85f, 0.85f, -1.0f, 0.8f, -0.85f, 0.85f, 1)

        // Cantilevered Upper Terraces (Asymmetric Brutalism)
        addBox(-1.25f, 0.5f, 0.8f, 1.35f, -0.7f, 1.1f, 1)
        addBox(-0.5f, 1.25f, 1.35f, 1.85f, -1.0f, 0.6f, 2)

        // Floating Crown Block
        addBox(-0.6f, 0.6f, 2.0f, 2.35f, -0.6f, 0.6f, 2)

        return ArchitecturalModel(verts, edges, faces, 2.4f)
    }

    /**
     * Geodesic Pavilion: Interlocking crystalline icosahedral nodes with radial
     * struts and responsive diaphragm facets.
     */
    private fun createGeodesicModel(): ArchitecturalModel {
        val verts = mutableListOf<Vec3>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        val phi = (1.0f + kotlin.math.sqrt(5.0f)) / 2.0f
        val scale = 1.3f

        // 12 base vertices of an icosahedron
        val baseCoords = listOf(
            Vec3(-1f, phi, 0f), Vec3(1f, phi, 0f), Vec3(-1f, -phi, 0f), Vec3(1f, -phi, 0f),
            Vec3(0f, -1f, phi), Vec3(0f, 1f, phi), Vec3(0f, -1f, -phi), Vec3(0f, 1f, -phi),
            Vec3(phi, 0f, -1f), Vec3(phi, 0f, 1f), Vec3(-phi, 0f, -1f), Vec3(-phi, 0f, 1f)
        )

        for (v in baseCoords) {
            verts.add(v.normalize() * scale)
        }

        // Subdivided geodesic midpoints
        val icoFaces = listOf(
            Triple(0, 11, 5), Triple(0, 5, 1), Triple(0, 1, 7), Triple(0, 7, 10), Triple(0, 10, 11),
            Triple(1, 5, 9), Triple(5, 11, 4), Triple(11, 10, 2), Triple(10, 7, 6), Triple(7, 1, 8),
            Triple(3, 9, 4), Triple(3, 4, 2), Triple(3, 2, 6), Triple(3, 6, 8), Triple(3, 8, 9),
            Triple(4, 9, 5), Triple(2, 4, 11), Triple(6, 2, 10), Triple(8, 6, 7), Triple(9, 8, 1)
        )

        val edgeMap = mutableMapOf<Pair<Int, Int>, Int>()
        fun getMidpoint(p1: Int, p2: Int): Int {
            val key = if (p1 < p2) Pair(p1, p2) else Pair(p2, p1)
            edgeMap[key]?.let { return it }
            val mid = ((verts[p1] + verts[p2]) * 0.5f).normalize() * scale
            val newIdx = verts.size
            verts.add(mid)
            edgeMap[key] = newIdx
            return newIdx
        }

        for (tri in icoFaces) {
            val m1 = getMidpoint(tri.first, tri.second)
            val m2 = getMidpoint(tri.second, tri.third)
            val m3 = getMidpoint(tri.third, tri.first)

            val subTris = listOf(
                Triple(tri.first, m1, m3),
                Triple(tri.second, m2, m1),
                Triple(tri.third, m3, m2),
                Triple(m1, m2, m3)
            )

            for (st in subTris) {
                edges.add(Edge3D(st.first, st.second, 1))
                edges.add(Edge3D(st.second, st.third, 1))
                edges.add(Edge3D(st.third, st.first, 1))

                val norm = (verts[st.second] - verts[st.first]).cross(verts[st.third] - verts[st.first]).normalize()
                faces.add(Face3D(st.first, st.second, st.third, norm, 1))
            }
        }

        return ArchitecturalModel(verts, edges, faces, 2.0f)
    }

    /**
     * 2D Chladni Surface: A planar acoustic vibration membrane with concentric wave rings
     * and nodal line matrices.
     */
    private fun createChladniModel(): ArchitecturalModel {
        val verts = mutableListOf<Vec3>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        val gridSize = 12
        val step = 3.2f / gridSize

        for (x in 0..gridSize) {
            for (z in 0..gridSize) {
                val px = -1.6f + x * step
                val pz = -1.6f + z * step
                // Initial Chladni nodal wave formula: cos(n*pi*x)*cos(m*pi*y) - cos(m*pi*x)*cos(n*pi*y)
                val dist = kotlin.math.sqrt(px * px + pz * pz)
                val py = sin(dist * 4.0f) * 0.18f
                verts.add(Vec3(px, py, pz))
            }
        }

        val rowStride = gridSize + 1
        for (x in 0 until gridSize) {
            for (z in 0 until gridSize) {
                val i0 = x * rowStride + z
                val i1 = (x + 1) * rowStride + z
                val i2 = (x + 1) * rowStride + (z + 1)
                val i3 = x * rowStride + (z + 1)

                edges.add(Edge3D(i0, i1, 0))
                edges.add(Edge3D(i0, i3, 0))

                val norm = Vec3(0f, 1f, 0f)
                faces.add(Face3D(i0, i1, i2, norm, 0))
                faces.add(Face3D(i0, i2, i3, norm, 0))
            }
        }

        return ArchitecturalModel(verts, edges, faces, 2.0f)
    }
}
