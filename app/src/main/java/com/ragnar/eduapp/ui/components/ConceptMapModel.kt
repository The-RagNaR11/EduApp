package com.ragnar.eduapp.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ragnar.eduapp.data.dataClass.GraphData
import com.ragnar.eduapp.ui.theme.AccentBlue
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.TextPrimary
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin


/**
 * Sample JSON
 * later implement through API
 */
val jsonString = """
{
  "visualization_type": "Concept Map",
  "main_concept": "True Joy in Life (George Bernard Shaw)",
  "nodes": [
    {"id": "A", "label": "Purpose", "category": "Core"},
    {"id": "B", "label": "Self-Recognition", "category": "Action"},
    {"id": "C", "label": "Force of Nature", "category": "Outcome"},
    {"id": "D", "label": "Selfish Clod", "category": "Core"},
    {"id": "E", "label": "Complaining", "category": "Action"}
  ],
  "edges": [
    {"from": "A", "to": "B", "label": "requires"},
    {"from": "A", "to": "C", "label": "leads to"},
    {"from": "C", "to": "D", "label": "contrast with"},
    {"from": "D", "to": "E", "label": "is associated with"}
  ]
}
"""


@Preview
@Composable
fun ConceptMapModel() {

    // Decode JSON string into GraphData object - happens only once due to remember
    val graphData = remember { Json.decodeFromString<GraphData>(jsonString) }

    // Create Paint object for rendering text on Canvas using native Android graphics
    val textPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true // Enable smooth text rendering
            textSize = 32f // Set text size in pixels
            color = android.graphics.Color.BLACK // Set text color to white
            textAlign = android.graphics.Paint.Align.CENTER // Center-align text
        }
    }

    // Function to assign colors based on node category for visual differentiation
    fun colorForCategory(category: String) = when (category) {
        "Core" -> Color(0xFF3B82F6)      // Blue for core concepts
        "Action" -> Color(0xFF10B981)    // Green for action items
        "Outcome" -> Color(0xFFF59E0B)   // Amber for outcomes
        else -> TextPrimary               // Default color for unknown categories
    }

    // Define node sizes in pixels
    val nodeRadius = 110f // Radius for regular nodes
    val mainConceptRadius = 150f // Larger radius for the main concept node

    // Store node positions as a map of node ID to Offset (x, y coordinates)
    // Using mutableStateMapOf to trigger recomposition when positions change
    val nodePositions = remember { mutableStateMapOf<String, Offset>() }

    // Track which node is currently selected (being dragged)
    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    // Zoom & pan state variables for canvas transformation
    var scale by remember { mutableStateOf(1f) } // Current zoom level (1f = 100%)
    var offset by remember { mutableStateOf(Offset.Zero) } // Pan offset from origin

    // Animate scale change when a node is selected - makes it slightly larger
    val animatedScale by animateFloatAsState(
        targetValue = if (selectedNodeId != null) 1.15f else 1f, // 15% larger when selected
        label = "node_scale_animation" // Label for debugging
    )

    // Store the canvas center point - needed for proper coordinate transformations
    var canvasCenter by remember { mutableStateOf(Offset.Zero) }

    /**
     * A box is used to contain both Canva and Zoom buttons
     */
    Box(modifier = Modifier.fillMaxSize()) {

        /**
         * The main canva Screen
         */
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)

                // TWO-FINGER GESTURES - For zoom and pan (must be first to detect multi-touch)

                /**
                 * to detect finger touch gesture
                 * detects two finger for zoom feature
                 */
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        // Updates zoom level, clamped between 50% and 300%
                        scale = (scale * zoom).coerceIn(0.5f, 3f)

                        // Update pan offset, adjusted for current zoom level
                        // This allows smooth panning that feels natural at any zoom level
                        offset += pan / scale
                    }
                }

                /**
                 * One finger for pan and move the nodes around
                 */
                .pointerInput(scale, offset, canvasCenter) { // re-runs the canva when there is any change in value
                    awaitEachGesture {
                        // waits for first touch
                        val down = awaitFirstDown()

                        // Convert screen touch coordinates to canvas coordinates
                        // Account for zoom (scale) and pan (offset)
                        val adjustedTouch = (down.position - offset - canvasCenter) / scale + canvasCenter

                        /**
                         * To check if any nodes is touched or not
                         */
                        val touchedNodeId = nodePositions.entries.find { (_, pos) ->
                            adjustedTouch.getDistanceTo(pos) <= nodeRadius
                        }?.key

                        /**
                         * If a node is touched then it can be dragged
                         */
                        if (touchedNodeId != null) {
                            selectedNodeId = touchedNodeId // mark the node as selected

                            // Track drag movements
                            drag(down.id) { change ->
                                // calculate how much user have moves the pointer i.e finger
                                val dragAmount = change.positionChange()

                                // updates the selected node position based on the drag
                                nodePositions[touchedNodeId]?.let { currentPosition ->
                                    // auto adjust drag amount based on zoom level
                                    nodePositions[touchedNodeId] = currentPosition + (dragAmount / scale)
                                }

                                // simple saves the changes to prevent any other gesture to happen
                                change.consume()
                            }

                            // end of selection
                            selectedNodeId = null
                        }
                    }
                }
        ) {
            // center of canva
            val center = Offset(size.width / 2, size.height / 2)
            canvasCenter = center

            /**
             * Runs only once and calculate initial position of the node
             */
            if (nodePositions.isEmpty()) {
                val totalNodes = graphData.nodes.size // Count total nodes
                val angleStep = (2 * Math.PI) / totalNodes // Divide circle into equal segments
                val radius = min(size.width, size.height) / 2.5f // Calculate layout radius

                // place each node around the middle/main node circle
                graphData.nodes.forEachIndexed { index, node ->
                    val angle = angleStep * index // Calculate angle for this node
                    nodePositions[node.id] = Offset(
                        (center.x + radius * cos(angle)).toFloat(), // X coordinate
                        (center.y + radius * sin(angle)).toFloat()  // Y coordinate
                    )
                }
            }

            // Apply zoom and pan transformations to all subsequent drawing operations
            withTransform({
                scale(scaleX = scale, scaleY = scale, pivot = center) // Apply zoom around center
                translate(left = offset.x, top = offset.y) // Apply pan offset
            }) {

                /**
                 * draws all the lines to connect the node i.e Edges
                 */
                graphData.edges.forEach { edge ->
                    val fromPos = nodePositions[edge.from] // Get start node position
                    val toPos = nodePositions[edge.to] // Get end node position

                    // check if both node exists or not
                    if (fromPos != null && toPos != null) {
                        //  a line to connect two node
                        drawLine(
                            color = Color.Gray,
                            start = fromPos,
                            end = toPos,
                            strokeWidth = 3.dp.toPx()
                        )

                        // calculate the center of edge to place the text
                        val midpoint = Offset(
                            (fromPos.x + toPos.x) / 2,
                            (fromPos.y + toPos.y) / 2
                        )

                        // place the text on the middle/center of each edge
                        drawIntoCanvas { canvas ->
                            val fontMetrics = textPaint.fontMetrics
                            // Adjust Y position to center text vertically
                            val textY = midpoint.y - (fontMetrics.ascent + fontMetrics.descent) / 2 - 8
                            canvas.nativeCanvas.drawText(edge.label, midpoint.x, textY, textPaint)
                        }
                    }
                }

                // draws the central node i.e. main node
                drawCircle(
                    color = Color(0xFF6366F1), // will change the color later
                    radius = mainConceptRadius,
                    center = center
                )

                // add the text inside the main node
                drawMultiLineText(
                    text = graphData.main_concept,
                    center = center,
                    textPaint = textPaint,
                    maxWidth = mainConceptRadius * 1.8f // fixed width for text wrapping
                )

                // draws all other nodes
                graphData.nodes.forEach { node ->
                    val pos = nodePositions[node.id] ?: return@forEach // Skip if no position

                    // Determine node color - highlighted if selected
                    val color = if (selectedNodeId == node.id)
                        colorForCategory(node.category).copy(alpha = 0.8f) // little transparent when selected
                    else
                        colorForCategory(node.category) // node color if not selected

                    // to grow node on selected for animation
                    val radius = nodeRadius * animatedScale

                    // different shape of node based on their category
                    when (node.category) {
                        "Core" -> {
                            // circular central node
                            drawCircle(color = color, radius = radius, center = pos)
                        }

                        "Action" -> {
                            // rectangle for action category
                            val width = radius * 1.8f // Make rectangle wider than tall
                            val height = radius * 1.1f
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(pos.x - width / 2, pos.y - height / 2), // Center the rect
                                size = Size(width, height),
                                cornerRadius = CornerRadius(40f, 40f) // Rounded corners
                            )
                        }

                        "Outcome" -> {
                            // hexagon for outcome category
                            val path = Path().apply {
                                val sides = 6 // no of side 6 for hexagon
                                for (i in 0 until sides) {
                                    val angle = (2 * Math.PI / sides * i).toFloat() // Calculate vertex angle
                                    val x = pos.x + radius * cos(angle) // X coordinate of vertex
                                    val y = pos.y + radius * sin(angle) // Y coordinate of vertex
                                    if (i == 0) moveTo(x, y) else lineTo(x, y) // Connect vertices
                                }
                                close() // closed the path to complete the shape
                            }
                            drawPath(path = path, color = color)
                        }

                        else -> {
                            // circle for any other category nodes
                            drawCircle(color = color, radius = radius, center = pos)
                        }
                    }

                    // add the text to each node
                    drawMultiLineText(
                        text = node.label,
                        center = pos,
                        textPaint = textPaint,
                        maxWidth = radius * 1f // fixed width based on node size
                    )
                }
            }
        }

        /**
         * A column with two button "+" and "-" for zoom control
         */
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            // Zoom In Button
            FloatingActionButton(
                onClick = {
                    scale = (scale * 1.2f).coerceIn(0.5f, 3f)
                    Log.d("ConceptMapModel", "Zoom in button clicked")
                },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                containerColor = AccentBlue,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = BackgroundPrimary
                )
            }

            // Spacing between buttons
            Spacer(modifier = Modifier.size(8.dp))

            // Zoom Out Button
            FloatingActionButton(
                onClick = {
                    scale = (scale / 1.2f).coerceIn(0.5f, 3f)
                    Log.d("ConceptMapModel", "Zoom out button clicked")
                },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                containerColor = Color(0xFF6366F1),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Helper method to add multi-line text inside node to fit the text inside the shape of node
 */
private fun DrawScope.drawMultiLineText(
    text: String,
    center: Offset,
    textPaint: android.graphics.Paint,
    maxWidth: Float
) {
    // Split text into words for line breaking
    val words = text.split(" ")
    val lines = mutableListOf<String>() // Store final lines
    var currentLine = "" // Build current line

    // Build lines that fit within maxWidth
    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"

        // Check if adding this word exceeds max width
        if (textPaint.measureText(testLine) > maxWidth) {
            lines.add(currentLine) // Add current line to list
            currentLine = word // Start new line with current word
        } else {
            currentLine = testLine // Continue building current line
        }
    }
    if (currentLine.isNotEmpty()) lines.add(currentLine) // Add final line

    // Calculate vertical positioning for centered multi-line text
    val fontMetrics = textPaint.fontMetrics
    val lineHeight = fontMetrics.descent - fontMetrics.ascent // Height of one line
    val totalHeight = lineHeight * lines.size // Total height of all lines
    val startY = center.y - totalHeight / 2 + lineHeight / 2 // Start Y to center vertically

    // Draw each line of text
    drawIntoCanvas { canvas ->
        lines.forEachIndexed { index, line ->
            // Calculate Y position for this line, adjusted for font metrics
            val y = startY + index * lineHeight - (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.nativeCanvas.drawText(line, center.x, y, textPaint)
        }
    }
}

/**
 * A helper method to calculate the distance method between two points using pythagoream theorem
 */
private fun Offset.getDistanceTo(other: Offset): Float =
    hypot(x - other.x, y - other.y) // sqrt((x2-x1)² + (y2-y1)²)