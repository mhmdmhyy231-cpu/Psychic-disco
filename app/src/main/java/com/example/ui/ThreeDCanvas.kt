package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.sin

data class Vertex3D(val x: Float, val y: Float, val z: Float)
data class Edge3D(val startIndex: Int, val endIndex: Int)

object Shapes3D {
    fun createPyramid(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(0f, -1.2f, 0f),      // 0: Peak
            Vertex3D(-1.1f, 0.7f, -1.1f), // 1: Base Front-Left
            Vertex3D(1.1f, 0.7f, -1.1f),  // 2: Base Front-Right
            Vertex3D(1.1f, 0.7f, 1.1f),   // 3: Base Back-Right
            Vertex3D(-1.1f, 0.7f, 1.1f)   // 4: Base Back-Left
        )
        val edges = listOf(
            Edge3D(0, 1), Edge3D(0, 2), Edge3D(0, 3), Edge3D(0, 4), // Sides
            Edge3D(1, 2), Edge3D(2, 3), Edge3D(3, 4), Edge3D(4, 1)  // Base
        )
        return Pair(vertices, edges)
    }

    fun createAnkh(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = mutableListOf<Vertex3D>()
        val edges = mutableListOf<Edge3D>()

        // 1. Loop at the top (circle in XY plane)
        val numPoints = 14
        val radius = 0.45f
        val centerY = -0.35f
        for (i in 0 until numPoints) {
            val angle = (2 * Math.PI * i / numPoints).toFloat()
            val x = radius * cos(angle)
            val y = centerY + radius * sin(angle)
            vertices.add(Vertex3D(x, y, 0f))
            edges.add(Edge3D(i, (i + 1) % numPoints))
        }

        // 2. Vertical stem
        val stemStartIdx = vertices.size
        vertices.add(Vertex3D(0f, 0.1f, 0f))
        vertices.add(Vertex3D(0f, 1.1f, 0f))
        edges.add(Edge3D(stemStartIdx, stemStartIdx + 1))

        // 3. Horizontal crossbar
        val crossStartIdx = vertices.size
        vertices.add(Vertex3D(-0.55f, 0.1f, 0f))
        vertices.add(Vertex3D(0.55f, 0.1f, 0f))
        edges.add(Edge3D(crossStartIdx, crossStartIdx + 1))

        // Add 3D thickness by cloning vertices and adding subtle depth offsets
        val initialSize = vertices.size
        for (i in 0 until initialSize) {
            val v = vertices[i]
            // Move original slightly forward
            vertices[i] = v.copy(z = 0.15f)
            // Add duplicate slightly backward
            vertices.add(v.copy(z = -0.15f))
            // Join back to front for 3D framework look!
            edges.add(Edge3D(i, initialSize + i))
        }

        // Add matching edges for the back layer
        for (edge in edges.toList()) {
            if (edge.startIndex < initialSize && edge.endIndex < initialSize) {
                edges.add(Edge3D(edge.startIndex + initialSize, edge.endIndex + initialSize))
            }
        }

        return Pair(vertices, edges)
    }

    fun createScarab(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = mutableListOf<Vertex3D>()
        val edges = mutableListOf<Edge3D>()

        // Draw a structured 3D beetle shell
        val stepsU = 6
        val stepsV = 8
        for (i in 0..stepsU) {
            val u = (Math.PI * i / stepsU).toFloat()
            for (j in 0 until stepsV) {
                val v = (2 * Math.PI * j / stepsV).toFloat()
                val x = 0.5f * sin(u) * cos(v)
                val y = 0.8f * cos(u)
                val z = 0.35f * sin(u) * sin(v)
                vertices.add(Vertex3D(x, y, z))
            }
        }

        // Connect the circular layers
        for (i in 0 until stepsU) {
            for (j in 0 until stepsV) {
                val current = i * stepsV + j
                val nextV = i * stepsV + ((j + 1) % stepsV)
                val nextU = (i + 1) * stepsV + j
                edges.add(Edge3D(current, nextV))
                if (i < stepsU) {
                    edges.add(Edge3D(current, nextU))
                }
            }
        }

        // Add 6 simple bug legs!
        val baseCount = vertices.size
        val legEndpoints = listOf(
            Pair(Vertex3D(-0.3f, -0.3f, 0f), Vertex3D(-0.75f, -0.4f, 0.2f)),
            Pair(Vertex3D(-0.35f, 0f, 0f), Vertex3D(-0.85f, 0f, 0.15f)),
            Pair(Vertex3D(-0.3f, 0.3f, 0f), Vertex3D(-0.75f, 0.4f, 0.2f)),
            Pair(Vertex3D(0.3f, -0.3f, 0f), Vertex3D(0.75f, -0.4f, 0.2f)),
            Pair(Vertex3D(0.35f, 0f, 0f), Vertex3D(0.85f, 0f, 0.15f)),
            Pair(Vertex3D(0.3f, 0.3f, 0f), Vertex3D(0.75f, 0.4f, 0.2f))
        )

        var currentLegIdx = baseCount
        for (leg in legEndpoints) {
            vertices.add(leg.first)
            vertices.add(leg.second)
            edges.add(Edge3D(currentLegIdx, currentLegIdx + 1))
            currentLegIdx += 2
        }

        return Pair(vertices, edges)
    }

    fun createSphinx(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            // Back Body
            Vertex3D(-0.45f, 0.4f, 0.6f),     // 0
            Vertex3D(0.45f, 0.4f, 0.6f),      // 1
            Vertex3D(0.45f, -0.1f, 0.6f),     // 2
            Vertex3D(-0.45f, -0.1f, 0.6f),    // 3
            // Front Chest
            Vertex3D(-0.45f, 0.4f, -0.3f),    // 4
            Vertex3D(0.45f, 0.4f, -0.3f),     // 5
            Vertex3D(0.45f, -0.1f, -0.3f),    // 6
            Vertex3D(-0.45f, -0.1f, -0.3f),   // 7
            // Crown Head
            Vertex3D(0f, -0.65f, -0.45f),     // 8: Peak
            Vertex3D(-0.25f, -0.4f, -0.45f),  // 9: Crown-Left
            Vertex3D(0.25f, -0.4f, -0.45f),   // 10: Crown-Right
            Vertex3D(0f, -0.25f, -0.55f),     // 11: Beard
            // Left & Right paws stretching forward
            Vertex3D(-0.35f, 0.4f, -0.85f),   // 12
            Vertex3D(0.35f, 0.4f, -0.85f)     // 13
        )

        val edges = listOf(
            // Draw carcass
            Edge3D(0, 1), Edge3D(1, 2), Edge3D(2, 3), Edge3D(3, 0),
            Edge3D(4, 5), Edge3D(5, 6), Edge3D(6, 7), Edge3D(7, 4),
            Edge3D(0, 4), Edge3D(1, 5), Edge3D(2, 6), Edge3D(3, 7),
            // Head and Beard
            Edge3D(8, 9), Edge3D(8, 10), Edge3D(9, 10),
            Edge3D(9, 11), Edge3D(10, 11),
            Edge3D(8, 6), Edge3D(11, 6), Edge3D(9, 7),
            // Paws
            Edge3D(4, 12), Edge3D(5, 13)
        )
        return Pair(vertices, edges)
    }

    // Omar Character Wireframe (Explorer Helper)
    fun createOmar(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(0f, -0.9f, 0.1f),       // 0: Hat peak
            Vertex3D(0f, -0.6f, -0.3f),      // 1: Hat brim front
            Vertex3D(0f, -0.6f, 0.3f),       // 2: Hat brim back
            Vertex3D(-0.3f, -0.6f, 0f),      // 3: Hat brim left
            Vertex3D(0.3f, -0.6f, 0f),       // 4: Hat brim right
            Vertex3D(0f, -0.3f, 0f),        // 5: Neck
            Vertex3D(0f, 0.4f, 0f),         // 6: Waist
            Vertex3D(-0.5f, -0.1f, -0.1f),   // 7: Left hand
            Vertex3D(0.5f, -0.1f, -0.1f),    // 8: Right hand
            Vertex3D(-0.25f, 1.0f, 0f),      // 9: Left foot
            Vertex3D(0.25f, 1.0f, 0f),       // 10: Right foot
            Vertex3D(-0.15f, 0.0f, 0.25f),   // 11: Backpack top-left
            Vertex3D(0.15f, 0.0f, 0.25f),    // 12: Backpack top-right
            Vertex3D(-0.15f, 0.3f, 0.25f),   // 13: Backpack bottom-left
            Vertex3D(0.15f, 0.3f, 0.25f)     // 14: Backpack bottom-right
        )
        val edges = listOf(
            Edge3D(0, 1), Edge3D(0, 2), Edge3D(0, 3), Edge3D(0, 4),
            Edge3D(1, 3), Edge3D(3, 2), Edge3D(2, 4), Edge3D(4, 1),
            Edge3D(5, 6), Edge3D(5, 7), Edge3D(5, 8),
            Edge3D(6, 9), Edge3D(6, 10),
            Edge3D(11, 12), Edge3D(12, 14), Edge3D(14, 13), Edge3D(13, 11),
            Edge3D(11, 5), Edge3D(12, 5), Edge3D(13, 6), Edge3D(14, 6)
        )
        return Pair(vertices, edges)
    }

    // Harrison Character Wireframe (Tourist lost in Sands)
    fun createHarrison(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(0f, -0.9f, -0.3f),      // 0: Cap front peak
            Vertex3D(0f, -0.8f, 0.1f),       // 1: Cap crown
            Vertex3D(0f, -0.5f, 0f),         // 2: Head
            Vertex3D(0f, -0.3f, 0f),         // 3: Neck
            Vertex3D(-0.35f, -0.2f, 0f),     // 4: Shoulder Left
            Vertex3D(0.35f, -0.2f, 0f),      // 5: Shoulder Right
            Vertex3D(0f, 0.4f, 0f),          // 6: Pelvis
            Vertex3D(-0.45f, 0.2f, 0.1f),    // 7: Left hand holding camera
            Vertex3D(0.45f, 0.2f, 0.1f),     // 8: Right hand holding camera
            Vertex3D(-0.25f, 1.0f, 0f),      // 9: Left foot
            Vertex3D(0.25f, 1.0f, 0f),       // 10: Right foot
            Vertex3D(-0.15f, -0.1f, -0.35f), // 11: Camera front-left
            Vertex3D(0.15f, -0.1f, -0.35f),  // 12: Camera front-right
            Vertex3D(-0.15f, 0.1f, -0.35f),  // 13: Camera back-left
            Vertex3D(0.15f, 0.1f, -0.35f)    // 14: Camera back-right
        )
        val edges = listOf(
            Edge3D(0, 1), Edge3D(1, 2), Edge3D(2, 3),
            Edge3D(3, 4), Edge3D(3, 5), Edge3D(4, 6), Edge3D(5, 6),
            Edge3D(4, 7), Edge3D(5, 8), Edge3D(6, 9), Edge3D(6, 10),
            Edge3D(11, 12), Edge3D(12, 14), Edge3D(14, 13), Edge3D(13, 11),
            Edge3D(11, 4), Edge3D(12, 5)
        )
        return Pair(vertices, edges)
    }

    // Anubis Character Wireframe (Jackal-Headed Deity)
    fun createAnubis(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(0f, -0.6f, -0.5f),      // 0: Nose tip
            Vertex3D(0f, -0.5f, -0.2f),      // 1: Snout base
            Vertex3D(0f, -0.5f, 0f),         // 2: Head central
            Vertex3D(-0.15f, -1.0f, 0.0f),   // 3: Ear tip Left
            Vertex3D(0.15f, -1.0f, 0.0f),    // 4: Ear tip Right
            Vertex3D(0f, -0.2f, 0f),         // 5: Neck
            Vertex3D(0f, 0.5f, 0f),          // 6: Waist
            Vertex3D(-0.35f, -0.1f, 0f),     // 7: Shoulder Left
            Vertex3D(0.35f, -0.1f, 0f),      // 8: Shoulder Right
            Vertex3D(-0.5f, 0.2f, -0.1f),    // 9: Left hand
            Vertex3D(0.5f, 0.1f, -0.2f),     // 10: Right hand holding staff
            Vertex3D(-0.25f, 1.0f, 0f),      // 11: Left foot
            Vertex3D(0.25f, 1.0f, 0f),       // 12: Right foot
            Vertex3D(0.5f, -0.9f, -0.2f),     // 13: Staff top
            Vertex3D(0.5f, 1.1f, -0.2f)       // 14: Staff bottom
        )
        val edges = listOf(
            Edge3D(0, 1), Edge3D(1, 2), Edge3D(2, 3), Edge3D(2, 4),
            Edge3D(2, 5), Edge3D(5, 7), Edge3D(5, 8), Edge3D(7, 6), Edge3D(8, 6),
            Edge3D(7, 9), Edge3D(8, 10), Edge3D(6, 11), Edge3D(6, 12),
            Edge3D(13, 14), Edge3D(10, 13)
        )
        return Pair(vertices, edges)
    }

    // Pharaoh Character Wireframe
    fun createPharaoh(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(0f, -1.0f, 0f),         // 0: Headdress peak
            Vertex3D(-0.45f, -0.3f, -0.1f),  // 1: Left flap
            Vertex3D(0.45f, -0.3f, -0.1f),   // 2: Right flap
            Vertex3D(0f, -0.2f, -0.2f),      // 3: Beard tip
            Vertex3D(0f, -0.2f, 0f),         // 4: Neck
            Vertex3D(-0.35f, -0.1f, 0f),     // 5: Shoulder L
            Vertex3D(0.35f, -0.1f, 0f),      // 6: Shoulder R
            Vertex3D(0f, 0.5f, 0f),          // 7: Waist
            Vertex3D(-0.2f, 0.1f, -0.3f),     // 8: Hand crossed Left
            Vertex3D(0.2f, 0.1f, -0.3f),      // 9: Hand crossed Right
            Vertex3D(-0.25f, 1.0f, 0f),      // 10: Foot Left
            Vertex3D(0.25f, 1.0f, 0f)        // 11: Foot Right
        )
        val edges = listOf(
            Edge3D(0, 1), Edge3D(0, 2), Edge3D(1, 4), Edge3D(2, 4), Edge3D(1, 2),
            Edge3D(3, 4), Edge3D(4, 5), Edge3D(4, 6), Edge3D(5, 7), Edge3D(6, 7),
            Edge3D(5, 8), Edge3D(6, 9), Edge3D(8, 9), Edge3D(7, 10), Edge3D(7, 11)
        )
        return Pair(vertices, edges)
    }

    // Tutankhamun Golden Mask / Sarcophagus (mummy)
    fun createMummy(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(0f, -1.0f, 0f),         // 0: Head apex
            Vertex3D(-0.45f, -0.5f, 0f),     // 1: Shoulder left
            Vertex3D(0.45f, -0.5f, 0f),      // 2: Shoulder right
            Vertex3D(-0.3f, 0.2f, 0f),       // 3: Waist left
            Vertex3D(0.3f, 0.2f, 0f),        // 4: Waist right
            Vertex3D(0f, 1.1f, 0f),          // 5: Feet base
            Vertex3D(0f, -1.0f, 0.3f),       // 6: Back apex
            Vertex3D(-0.45f, -0.5f, 0.3f),   // 7: Back shoulder left
            Vertex3D(0.45f, -0.5f, 0.3f),    // 8: Back shoulder right
            Vertex3D(-0.3f, 0.2f, 0.3f),     // 9: Back waist left
            Vertex3D(0.3f, 0.2f, 0.3f),      // 10: Back waist right
            Vertex3D(0f, 1.1f, 0.3f)         // 11: Back feet base
        )
        val edges = listOf(
            // Front shell
            Edge3D(0, 1), Edge3D(0, 2), Edge3D(1, 3), Edge3D(2, 4), Edge3D(3, 5), Edge3D(4, 5),
            // Back shell
            Edge3D(6, 7), Edge3D(6, 8), Edge3D(7, 9), Edge3D(8, 10), Edge3D(9, 11), Edge3D(10, 11),
            // Joins for 3D depth
            Edge3D(0, 6), Edge3D(1, 7), Edge3D(2, 8), Edge3D(3, 9), Edge3D(4, 10), Edge3D(5, 11)
        )
        return Pair(vertices, edges)
    }

    // Royal Cartouche (cartouche)
    fun createCartouche(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(-0.3f, -0.9f, 0f),      // 0
            Vertex3D(0.3f, -0.9f, 0f),       // 1
            Vertex3D(0.35f, -0.6f, 0f),      // 2
            Vertex3D(0.35f, 0.6f, 0f),       // 3
            Vertex3D(0.3f, 0.9f, 0f),        // 4
            Vertex3D(-0.3f, 0.9f, 0f),       // 5
            Vertex3D(-0.35f, 0.6f, 0f),      // 6
            Vertex3D(-0.35f, -0.6f, 0f),     // 7
            // Hieroglyphic horizontal eye symbol inside (Eye of Horus)
            Vertex3D(-0.2f, -0.2f, 0f),      // 8
            Vertex3D(0.2f, -0.2f, 0f),       // 9
            Vertex3D(0f, -0.3f, 0f),         // 10
            Vertex3D(0f, -0.1f, 0f)          // 11
        )
        val edges = listOf(
            Edge3D(0, 1), Edge3D(1, 2), Edge3D(2, 3), Edge3D(3, 4),
            Edge3D(4, 5), Edge3D(5, 6), Edge3D(6, 7), Edge3D(7, 0),
            // Inner Horus Eye
            Edge3D(8, 10), Edge3D(10, 9), Edge3D(9, 11), Edge3D(11, 8), Edge3D(8, 9)
        )
        return Pair(vertices, edges)
    }

    // Stellar Obelisk (obelisk)
    fun createObelisk(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(0f, -1.4f, 0f),         // 0: Peak
            Vertex3D(-0.15f, -0.8f, -0.15f), // 1: Upper top front-left
            Vertex3D(0.15f, -0.8f, -0.15f),  // 2: Upper top front-right
            Vertex3D(0.15f, -0.8f, 0.15f),   // 3: Upper top back-right
            Vertex3D(-0.15f, -0.8f, 0.15f),  // 4: Upper top back-left
            Vertex3D(-0.3f, 1.1f, -0.3f),    // 5: Base bottom front-left
            Vertex3D(0.3f, 1.1f, -0.3f),     // 6: Base bottom front-right
            Vertex3D(0.3f, 1.1f, 0.3f),      // 7: Base bottom back-right
            Vertex3D(-0.3f, 1.1f, 0.3f)      // 8: Base bottom back-left
        )
        val edges = listOf(
            // Pyramidion top
            Edge3D(0, 1), Edge3D(0, 2), Edge3D(0, 3), Edge3D(0, 4),
            Edge3D(1, 2), Edge3D(2, 3), Edge3D(3, 4), Edge3D(4, 1),
            // Columns
            Edge3D(1, 5), Edge3D(2, 6), Edge3D(3, 7), Edge3D(4, 8),
            // Base
            Edge3D(5, 6), Edge3D(6, 7), Edge3D(7, 8), Edge3D(8, 5)
        )
        return Pair(vertices, edges)
    }

    // Ancient Scroll / Papyri (scroll)
    fun createScroll(): Pair<List<Vertex3D>, List<Edge3D>> {
        val vertices = listOf(
            Vertex3D(-0.5f, -0.7f, -0.15f),  // 0: Left spool top
            Vertex3D(-0.5f, 0.7f, -0.15f),   // 1: Left spool bottom
            Vertex3D(-0.4f, -0.7f, 0.15f),   // 2
            Vertex3D(-0.4f, 0.7f, 0.15f),    // 3
            Vertex3D(0.4f, -0.7f, 0.15f),    // 4
            Vertex3D(0.4f, 0.7f, 0.15f),     // 5
            Vertex3D(0.5f, -0.7f, -0.15f),   // 6: Right spool top
            Vertex3D(0.5f, 0.7f, -0.15f)     // 7: Right spool bottom
        )
        val edges = listOf(
            Edge3D(0, 1), Edge3D(2, 3), Edge3D(4, 5), Edge3D(6, 7),
            Edge3D(0, 2), Edge3D(1, 3), Edge3D(2, 4), Edge3D(3, 5),
            Edge3D(4, 6), Edge3D(5, 7)
        )
        return Pair(vertices, edges)
    }
}

@Composable
fun ThreeDObjectRenderer(
    relicId: String,
    rotationX: Float,
    rotationY: Float,
    onDrag: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFD700) // Golden Amber
) {
    val (vertices, edges) = remember(relicId) {
        when (relicId) {
            "scarab" -> Shapes3D.createScarab()
            "ankh" -> Shapes3D.createAnkh()
            "sphinx" -> Shapes3D.createSphinx()
            "pyramid" -> Shapes3D.createPyramid()
            "mummy" -> Shapes3D.createMummy()
            "cartouche" -> Shapes3D.createCartouche()
            "obelisk" -> Shapes3D.createObelisk()
            "scroll" -> Shapes3D.createScroll()
            "omar" -> Shapes3D.createOmar()
            "harrison" -> Shapes3D.createHarrison()
            "anubis" -> Shapes3D.createAnubis()
            "pharaoh" -> Shapes3D.createPharaoh()
            else -> Shapes3D.createPyramid()
        }
    }


    Canvas(
        modifier = modifier
            .pointerInput(relicId) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Map drag offset to rotation angles
                    val rxFactor = -dragAmount.y * 0.4f
                    val ryFactor = dragAmount.x * 0.4f
                    onDrag(rxFactor, ryFactor)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val minDim = kotlin.math.min(width, height)
        val scale = minDim * 0.45f // Scale 3D representation to container coordinates
        val cameraDistance = 3.2f

        val radX = Math.toRadians(rotationX.toDouble()).toFloat()
        val radY = Math.toRadians(rotationY.toDouble()).toFloat()

        // 3D Trigonometry lookup
        val cosX = cos(radX)
        val sinX = sin(radX)
        val cosY = cos(radY)
        val sinY = sin(radY)

        // Project 3D points to 2D screen offsets with robust division-by-zero & infinity protections
        val projectedPoints = vertices.map { vertex ->
            // 1. Rotate around X-axis
            val y1 = vertex.y * cosX - vertex.z * sinX
            val z1 = vertex.y * sinX + vertex.z * cosX

            // 2. Rotate around Y-axis
            val x2 = vertex.x * cosY + z1 * sinY
            val z2 = -vertex.x * sinY + z1 * cosY

            // 3. Perspective calculation with safe division safeguard
            val rawDenom = z2 + cameraDistance
            val denom = if (rawDenom.isNaN() || rawDenom <= 0.05f) 0.05f else rawDenom
            
            val screenX = (width / 2f) + (x2 * scale / denom)
            val screenY = (height / 2f) + (y1 * scale / denom)

            if (screenX.isFinite() && screenY.isFinite()) {
                Offset(screenX, screenY)
            } else {
                Offset(width / 2f, height / 2f)
            }
        }

        // Draw structural wireframe edges
        for (edge in edges) {
            val p1 = projectedPoints.getOrNull(edge.startIndex)
            val p2 = projectedPoints.getOrNull(edge.endIndex)
            if (p1 != null && p2 != null) {
                // Outer glow stroke and fine inner stroke
                drawLine(
                    color = color.copy(alpha = 0.25f),
                    start = p1,
                    end = p2,
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = p1,
                    end = p2,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Highlight vertices with glowing circular markers
        for (pt in projectedPoints) {
            drawCircle(
                color = color,
                radius = 5f,
                center = pt
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = 2f,
                center = pt
            )
        }
    }
}
