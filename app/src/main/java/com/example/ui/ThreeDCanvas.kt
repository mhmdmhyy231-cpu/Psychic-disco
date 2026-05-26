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
