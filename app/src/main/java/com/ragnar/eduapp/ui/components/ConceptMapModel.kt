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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import com.ragnar.eduapp.data.dataClass.GraphData
import com.ragnar.eduapp.data.dataClass.GraphData.AudioSegment
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.textFieldBackgroundColor
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.GraphUtils
import kotlinx.serialization.json.Json
import kotlin.math.hypot

/**
 * ConceptMapModel - Audio-Synchronized Knowledge Graph Visualization
 *
 * This composable renders an interactive concept map that synchronizes with audio playback.
 * It implements dynamic node visibility and highlighting based on the current audio position.
 *
 * @param json JSON string containing graph data (nodes, edges, audioSegments)
 * @param currentAudioTime Current audio playback position in seconds
 * @param isAudioPlaying Boolean indicating if audio is currently playing
 *
 * Features:
 * - Audio-synchronized node/edge visibility
 * - Progressive rendering based on audio segments
 * - Highlighting of active nodes and edges
 * - Pan and zoom gestures
 * - Node dragging
 * - Hierarchical layout
 */
@Composable
fun ConceptMapModel(
    json: String,
    currentAudioTime: Float = 0f,
    isAudioPlaying: Boolean = false
) {

    val TAG = "ConceptMapModel"
    val graphUtils = GraphUtils()

    /**
     * JSON Parsing with Error Handling
     * Converts JSON string into GraphData object using Kotlin Serialization
     * Falls back to default graph if parsing fails
     */
    val graphData = remember(json) {
        try {
            DebugLogger.debugLog(TAG, "Raw JSON received: $json")
            val parsedData = Json.decodeFromString<GraphData>(json)
            
            // Compute startTime and endTime for audio segments if not provided
            val processedAudioSegments = parsedData.audioSegments.mapIndexed { index, segment ->
                if (segment.startTime == 0f && segment.endTime == 0f) {
                    // Compute times based on previous segments
                    val previousSegments = parsedData.audioSegments.take(index)
                    segment.computeTimes(previousSegments)
                } else {
                    segment
                }
            }
            
            val processedData = parsedData.copy(audioSegments = processedAudioSegments)
            
            DebugLogger.debugLog(TAG, "Successfully parsed GraphData:")
            DebugLogger.debugLog(TAG, "  - Main concept: ${processedData.main_concept}")
            DebugLogger.debugLog(TAG, "  - Nodes count: ${processedData.nodes.size}")
            DebugLogger.debugLog(TAG, "  - Edges count: ${processedData.edges.size}")
            DebugLogger.debugLog(TAG, "  - Audio segments count: ${processedData.audioSegments.size}")
            
            // Log computed audio segment times
            processedData.audioSegments.forEachIndexed { index, segment ->
                DebugLogger.debugLog(TAG, "  - Segment $index: ${segment.startTime}s-${segment.endTime}s (${segment.estimatedDuration}s)")
            }
            
            processedData
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error parsing JSON: $e")
            DebugLogger.errorLog(TAG, "JSON content: $json")
            graphUtils.createDefaultGraphData()
        }
    }

    /**
     * Paint objects for different text elements
     * Android native Paint is required for Canvas text rendering
     * asFrameworkPaint() converts Compose Color to Android native Paint
     */
    val titlePaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 56f
            color = TextPrimary.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    val textPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 28f
            color = TextPrimary.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    val edgeLabelPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 23f
            color = TextSecondary.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    /**
     * Category-based color mapping for nodes
     * Different concepts get different colors for visual distinction
     */
    fun colorForCategory(category: String) = when (category) {
        "Core", "Main" -> Color(0xFF3B82F6)
        "Action" -> Color(0xFF10B981)
        "Outcome" -> Color(0xFFF59E0B)
        "Application" -> Color(0xFFEC4899)
        "Biological Feature" -> Color(0xFF8B5CF6)
        "Social Structure" -> Color(0xFF14B8A6)
        "History" -> Color(0xFF6366F1)
        "Biology" -> Color(0xFF10B981)
        "Nutrition" -> Color(0xFF84CC16)
        "Behavior" -> Color(0xFF06B6D4)
        "Health" -> Color(0xFFF43F5E)
        "Ecology" -> Color(0xFF22C55E)
        "Genetics" -> Color(0xFFA855F7)
        "Taxonomy" -> Color(0xFF3B82F6)
        "Agriculture" -> Color(0xFFF97316)
        "Secondary" -> Color(0xFF8B5CF6)
        "Leaf" -> Color(0xFF10B981)
        else -> Color(0xFF64748B)
    }

    /**
     * State variables for visualization control
     */
    val nodeRadius = 90f

    // Maps node IDs to their screen positions (Offset)
    // Recalculated when JSON changes
    val nodePosition = remember(json) { mutableStateMapOf<String, Offset>() }

    // Tracks which node is being dragged (null = none)
    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    // Zoom level (1f = normal, 2f = 2x zoom, 0.25f = 25% zoom)
    var scale by remember { mutableFloatStateOf(1f) }

    // Pan offset for moving the entire canvas
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Canvas center point
    var canvasCenter by remember { mutableStateOf(Offset.Zero) }

    /**
     * =================================================================
     * AUDIO SYNCHRONIZATION LOGIC - Dynamic Node Visibility Check
     * =================================================================
     *
     * This section implements the core feature from the document:
     * "The system continuously checks the synchronization map against
     * the audio playback position"
     *
     * Event Trigger: When audio time enters segment range (Tstart < T < Tend)
     * Node/Edge Lookup: Find elements associated with current segment
     * Visualization Action: Show, hide, or highlight elements
     */

    /**
     * Find the current active audio segment based on playback time
     * Returns null if no segment is active
     *
     * Logic: Tstart <= currentTime <= Tend
     */
    val currentSegment = remember(currentAudioTime.toInt(), graphData.audioSegments) {
        if (isAudioPlaying && currentAudioTime > 0f) {
            graphData.audioSegments.find { segment ->
                currentAudioTime >= segment.startTime &&
                        currentAudioTime <= segment.endTime
            }
        } else {
            null
        }
    }

    /**
     * Build cumulative visibility set - nodes that should be visible
     * Based on all segments up to and including the current one
     *
     * This ensures nodes remain visible once introduced
     * Example: Segment 0 shows [A], Segment 1 shows [B, C]
     *          At Segment 1, visible nodes = [A, B, C]
     */
    val visibleNodeIds = remember(currentSegment, graphData.audioSegments, isAudioPlaying) {
        val visible = mutableSetOf<String>()

        if (currentSegment != null) {
            // Get index of current segment
            val currentIndex = graphData.audioSegments.indexOf(currentSegment)

            // Accumulate all showNodeIds from segment 0 to current
            graphData.audioSegments.take(currentIndex + 1).forEach { segment ->
                visible.addAll(segment.showNodeIds)
            }
        } else if (!isAudioPlaying) {
            // When not playing, show all nodes (fallback to normal view)
            visible.addAll(graphData.nodes.map { it.id })
        }

        visible
    }

    /**
     * Get nodes to highlight in current segment
     * These nodes get visual emphasis (thicker border, pulse effect)
     */
    val highlightedNodeIds = remember(currentSegment) {
        currentSegment?.highlightNodeIds?.toSet() ?: emptySet()
    }

    /**
     * Get edges to highlight in current segment
     * These edges get emphasized (thicker line, brighter color)
     */
    val highlightedEdgeIds = remember(currentSegment) {
        currentSegment?.highlightEdgeIds?.toSet() ?: emptySet()
    }

    /**
     * Debug logging for audio synchronization
     * Logs current state when segment changes
     */
    LaunchedEffect(currentSegment) {
        currentSegment?.let { segment ->
            DebugLogger.debugLog(TAG, """
                ════════════════════════════════════════════════════════
                AUDIO SYNC - Segment Active
                ════════════════════════════════════════════════════════
                Time: ${currentAudioTime}s (${segment.startTime}s - ${segment.endTime}s)
                Spoken: "${segment.spokenText}"
                Action: ${segment.action}
                ────────────────────────────────────────────────────────
                Visible Nodes: ${visibleNodeIds.size} → $visibleNodeIds
                Highlighted Nodes: ${highlightedNodeIds.size} → $highlightedNodeIds
                Highlighted Edges: ${highlightedEdgeIds.size} → $highlightedEdgeIds
                ════════════════════════════════════════════════════════
            """.trimIndent())
        } ?: run {
            DebugLogger.debugLog(TAG, "No active audio segment at time ${currentAudioTime}s")
        }
    }
    
    /**
     * Log audio segments data for debugging
     */
    LaunchedEffect(graphData.audioSegments) {
        DebugLogger.debugLog(TAG, "Audio segments loaded: ${graphData.audioSegments.size} segments")
        graphData.audioSegments.forEachIndexed { index, segment ->
            DebugLogger.debugLog(TAG, "Segment $index: ${segment.startTime}s-${segment.endTime}s, action: ${segment.action}")
        }
    }

    /**
     * Smooth animation when a node is selected (dragged)
     * Selected nodes grow to 115% of normal size
     */
    val animationScale by animateFloatAsState(
        targetValue = if (selectedNodeId != null) 1.15f else 1f,
        label = "node_scale_animation"
    )

    /**
     * Main UI Layout
     * Box contains:
     * 1. Canvas (graph rendering)
     * 2. Zoom controls (floating buttons)
     */
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        /**
         * ═══════════════════════════════════════════════════════════
         * CANVAS - Graph Rendering Surface
         * ═══════════════════════════════════════════════════════════
         */
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)

                /**
                 * Gesture Handler: Pinch-to-zoom
                 * Two-finger pinch gesture for zooming
                 * Zoom range: 25% to 400%
                 */
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.25f, 4f)
                        offset += pan / scale
                    }
                }

                /**
                 * Gesture Handler: Node dragging
                 * Single-finger drag to reposition nodes
                 *
                 * Steps:
                 * 1. Detect finger down
                 * 2. Convert touch position to graph space
                 * 3. Find if touch is within any node's radius
                 * 4. Track finger movement and update node position
                 * 5. Clear selection on finger up
                 */
                .pointerInput(scale, offset, canvasCenter) {
                    awaitEachGesture {
                        val down = awaitFirstDown()

                        // Convert touch position from screen space to graph space
                        val adjustedTouch = (down.position - offset - canvasCenter) / scale + canvasCenter

                        // Find touched node (if any)
                        val touchedNodeId = nodePosition.entries.find { (_, pos) ->
                            adjustedTouch.getDistanceTo(pos) <= nodeRadius
                        }?.key

                        if (touchedNodeId != null) {
                            selectedNodeId = touchedNodeId

                            // Track drag movement
                            drag(down.id) { change ->
                                val dragAmount = change.positionChange()
                                nodePosition[touchedNodeId]?.let { currentPosition ->
                                    nodePosition[touchedNodeId] = currentPosition + (dragAmount / scale)
                                }
                                change.consume()
                            }

                            selectedNodeId = null
                        }
                    }
                }
        ) {
            // Calculate and store canvas center
            val center = Offset(size.width / 2, size.height / 2)
            canvasCenter = center

            /**
             * Safety check - exit if no data
             */
            if (graphData.nodes.isEmpty()) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText("No data found", center.x, center.y, textPaint)
                }
                return@Canvas
            }

            /**
             * ═══════════════════════════════════════════════════════
             * HIERARCHICAL LAYOUT ALGORITHM
             * ═══════════════════════════════════════════════════════
             *
             * Only runs once when positions are not yet calculated
             * Creates a tree-like structure with levels:
             * Level 0: Root nodes (no incoming edges)
             * Level 1: Children of root
             * Level 2: Grandchildren, etc.
             */
            if (nodePosition.isEmpty()) {
                // Build adjacency list: Map<ParentId, List<ChildrenIds>>
                val adjacencyList = mutableMapOf<String, MutableList<String>>()
                val inDegree = mutableMapOf<String, Int>()

                graphData.nodes.forEach { node ->
                    adjacencyList[node.id] = mutableListOf()
                    inDegree[node.id] = 0
                }

                graphData.edges.forEach { edge ->
                    adjacencyList[edge.from]?.add(edge.to)
                    inDegree[edge.to] = (inDegree[edge.to] ?: 0) + 1
                }

                // Find root nodes (inDegree = 0)
                val rootNodes = graphData.nodes.filter { (inDegree[it.id] ?: 0) == 0 }
                val roots = if (rootNodes.isEmpty()) listOf(graphData.nodes.first()) else rootNodes

                // Layout parameters
                val startY = 180f
                val levelHeight = 380f
                val horizontalSpacing = 350f

                /**
                 * BFS (Breadth-First Search) for level assignment
                 * Assigns each node a level number (depth in tree)
                 */
                val levels = mutableMapOf<String, Int>()
                val queue = ArrayDeque<Pair<String, Int>>()
                val visited = mutableSetOf<String>()

                roots.forEach { root ->
                    queue.add(root.id to 0)
                    visited.add(root.id)
                }

                while (queue.isNotEmpty()) {
                    val (nodeId, level) = queue.removeFirst()
                    levels[nodeId] = level

                    adjacencyList[nodeId]?.forEach { childId ->
                        if (childId !in visited) {
                            visited.add(childId)
                            queue.add(childId to level + 1)
                        }
                    }
                }

                /**
                 * Position nodes based on their level
                 * Each level is centered horizontally
                 */
                val nodesByLevel = levels.entries.groupBy({ it.value }, { it.key })

                nodesByLevel.forEach { (level, nodesInLevel) ->
                    val y = startY + level * levelHeight
                    val levelWidth = (nodesInLevel.size - 1) * horizontalSpacing
                    val startX = center.x - levelWidth / 2

                    nodesInLevel.forEachIndexed { index, nodeId ->
                        val x = if (nodesInLevel.size == 1) {
                            center.x
                        } else {
                            startX + index * horizontalSpacing
                        }
                        nodePosition[nodeId] = Offset(x, y)
                    }
                }
            }

            /**
             * ═══════════════════════════════════════════════════════
             * APPLY ZOOM AND PAN TRANSFORMATION
             * ═══════════════════════════════════════════════════════
             * All subsequent drawing is transformed
             */
            withTransform({
                scale(scaleX = scale, scaleY = scale, pivot = center)
                translate(left = offset.x, top = offset.y)
            }) {
                /**
                 * ═══════════════════════════════════════════════════
                 * DRAW EDGES (Relationships between nodes)
                 * ═══════════════════════════════════════════════════
                 *
                 * AUDIO SYNC: Only draw edges if both endpoints are visible
                 * AUDIO SYNC: Highlight edges if in highlightedEdgeIds
                 */
                graphData.edges.forEach { edge ->
                    val fromPos = nodePosition[edge.from]
                    val toPos = nodePosition[edge.to]

                    // Skip edge if either endpoint is not visible
                    if (!visibleNodeIds.contains(edge.from) ||
                        !visibleNodeIds.contains(edge.to)) {
                        return@forEach
                    }

                    if (fromPos != null && toPos != null) {
                        val isHighlighted = highlightedEdgeIds.contains(edge.id)

                        // Create curved path (quadratic Bezier curve)
                        val path = Path().apply {
                            moveTo(fromPos.x, fromPos.y)
                            val controlX = (fromPos.x + toPos.x) / 2
                            val controlY = fromPos.y + (toPos.y - fromPos.y) * 0.3f
                            quadraticTo(controlX, controlY, toPos.x, toPos.y)
                        }

                        // Draw edge with highlighting
                        drawPath(
                            path,
                            color = if (isHighlighted) {
                                Color(0xFF3B82F6).copy(alpha = 1f) // Bright blue when highlighted
                            } else {
                                Color(0xFF6B7280).copy(alpha = 0.6f) // Gray when normal
                            },
                            style = Stroke(width = if (isHighlighted) 5.dp.toPx() else 3.dp.toPx())
                        )

                        // Draw edge label
                        val midX = (fromPos.x + toPos.x) / 2
                        val midY = (fromPos.y + toPos.y) / 2 - 30

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(edge.label, midX, midY, edgeLabelPaint)
                        }
                    }
                }

                /**
                 * ═══════════════════════════════════════════════════
                 * DRAW NODES
                 * ═══════════════════════════════════════════════════
                 *
                 * AUDIO SYNC: Only draw nodes if in visibleNodeIds
                 * AUDIO SYNC: Highlight nodes if in highlightedNodeIds
                 * AUDIO SYNC: Add pulse effect to highlighted nodes
                 */
                graphData.nodes.forEach { node ->
                    // Skip node if not visible
                    if (!visibleNodeIds.contains(node.id)) {
                        return@forEach
                    }

                    val pos = nodePosition[node.id] ?: return@forEach

                    val isHighlighted = highlightedNodeIds.contains(node.id)
                    val isSelected = selectedNodeId == node.id

                    // Node color with highlighting
                    val color = when {
                        isSelected -> colorForCategory(node.category).copy(alpha = 0.9f)
                        isHighlighted -> colorForCategory(node.category).copy(alpha = 1f)
                        else -> colorForCategory(node.category).copy(alpha = 0.8f)
                    }

                    // Node radius with selection/highlight scaling
                    val radius = when {
                        isSelected -> nodeRadius * animationScale
                        isHighlighted -> nodeRadius * 1.08f // Slightly larger when highlighted
                        else -> nodeRadius
                    }

                    // Drop shadow for depth
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.15f),
                        radius = radius,
                        center = pos + Offset(4f, 4f)
                    )

                    // Main node circle
                    drawCircle(color, radius, center = pos)

                    // Highlight ring for audio-synced nodes
                    if (isHighlighted) {
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.5f), // Golden glow
                            radius = radius + 12f,
                            center = pos,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }

                    // Inner highlight circle (for shine effect)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = radius * 0.85f,
                        center = pos
                    )

                    // Node label text
                    drawMultiLineText(
                        text = node.label,
                        center = pos,
                        textPaint = textPaint,
                        maxWidth = radius * 1.6f
                    )
                }
            }

            /**
             * Draw title (not affected by zoom/pan)
             * Always visible at top of canvas
             */
            drawIntoCanvas { canvas ->
                val titleY = 60f
                canvas.nativeCanvas.drawText(
                    graphData.main_concept,
                    center.x,
                    titleY,
                    titlePaint
                )
            }
        }

        /**
         * ═══════════════════════════════════════════════════════════
         * ZOOM CONTROLS - Floating Action Buttons
         * ═══════════════════════════════════════════════════════════
         */
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            // Zoom In Button (+)
            FloatingActionButton(
                onClick = {
                    scale = (scale * 1.25f).coerceIn(0.25f, 4f)
                    DebugLogger.debugLog(TAG, "Zoom In: scale = $scale")
                },
                modifier = Modifier.size(37.dp),
                shape = RectangleShape,
                containerColor = textFieldBackgroundColor,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            // Zoom Out Button (-)
            FloatingActionButton(
                onClick = {
                    scale = (scale / 1.25f).coerceIn(0.25f, 4f)
                    DebugLogger.debugLog(TAG, "Zoom Out: scale = $scale")
                },
                modifier = Modifier.size(37.dp),
                shape = RectangleShape,
                containerColor = textFieldBackgroundColor,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = TextSecondary
                )
            }
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * HELPER FUNCTIONS
 * ═══════════════════════════════════════════════════════════════════
 */

/**
 * Draw multi-line text within a maximum width
 * Wraps words to fit within the specified width
 *
 * Algorithm:
 * 1. Split text into words
 * 2. Build lines by adding words until maxWidth is exceeded
 * 3. Start new line when overflow occurs
 * 4. Draw each line with proper vertical spacing
 *
 * @param text Text to draw
 * @param center Center position for text
 * @param textPaint Paint object with text styling
 * @param maxWidth Maximum width before wrapping
 */
fun DrawScope.drawMultiLineText(
    text: String,
    center: Offset,
    textPaint: android.graphics.Paint,
    maxWidth: Float
) {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""

    // Build lines
    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (textPaint.measureText(testLine) > maxWidth) {
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
            currentLine = word
        } else {
            currentLine = testLine
        }
    }
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }

    // Draw lines
    val lineHeight = textPaint.textSize * 1.2f
    val totalHeight = lines.size * lineHeight
    val startY = center.y - (totalHeight / 2) + (lineHeight / 2)

    drawIntoCanvas { canvas ->
        lines.forEachIndexed { index, line ->
            val y = startY + (index * lineHeight)
            canvas.nativeCanvas.drawText(line, center.x, y, textPaint)
        }
    }
}

/**
 * Calculate distance between two points
 * Uses Pythagorean theorem: d = √((x2-x1)² + (y2-y1)²)
 *
 * @param other Target point
 * @return Distance in pixels
 */
private fun Offset.getDistanceTo(other: Offset): Float =
    hypot(x - other.x, y - other.y)