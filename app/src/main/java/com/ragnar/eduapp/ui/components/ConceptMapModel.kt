package com.ragnar.eduapp.ui.components

import android.util.Log
import androidx.compose.animation.core.Animation
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
import com.ragnar.eduapp.utils.GraphUtils
import kotlinx.serialization.json.Json
import kotlin.math.hypot

@Composable
fun ConceptMapModel(
    json: String,
    currentAudioTime: Float = 0f,
    isAudioPlaying: Boolean = false
) {

    val TAG: String = "ConceptMapModel"

    val graphUtils: GraphUtils = GraphUtils()

    /**
     * JSON Parsing with Error Handling
     */
    val graphData = remember(json) {
        try {
            Json.decodeFromString<GraphData>(json) // converts json into GraphData Object using kotlin Serialization
        }catch (e: Exception) {
            Log.e(TAG, "Error: error while parsing JSON: $e")
            graphUtils.createDefaultGraphData() // if any error occurs then return the default graph data
        }
    }

    /**
     * Different paint object for
     * 1. title
     * 2. Node text
     * 3. edge label
     * Reason to have this
     * Canvas object require Android native Paint object
     * asFrameworkPaint() is used to convert compose Color into Android native Paint
     */

    val titlePaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true // smooth Edges
            textSize = 56f // large text for title
            color = TextPrimary.toArgb() // text color
            textAlign = android.graphics.Paint.Align.CENTER  // center alignment
            isFakeBoldText = true // bold text
        }
    }
    val textPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true // smooth Edges
            textSize = 28f // smaller text for text inside nodes
            color = TextPrimary.toArgb() // text color
            textAlign = android.graphics.Paint.Align.CENTER  // center alignment
        }
    }
    val edgeLabelPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true // smooth Edges
            textSize = 23f // even smaller text for edge label
            color = TextSecondary.toArgb() // text color
            textAlign = android.graphics.Paint.Align.CENTER  // center alignment
        }
    }

    /**
     * Different color for different category of nodes
     */
    fun colorForCategory(category: String) = when (category) {
        "Core" -> Color(0xFF3B82F6)
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
        else -> Color(0xFF64748B)
    }

    /**
     * Different state variables
     */
    val nodeRadius = 90f
    // used to map node id with screen coordinates
    // resets when node changes using remember(json)
    val nodePosition = remember(json) { mutableStateMapOf<String, Offset>() }
    var selectedNodeId by remember { mutableStateOf<String?>(null) } // track which node is being dragged (null mean none)
    var scale by remember { mutableFloatStateOf(1f) } // track zoom level (1f = normal, 2f = 2x zoom)
    var offset by remember { mutableStateOf(Offset.Zero) } // pan position for moving the entire screen
    var canvasCenter by remember { mutableStateOf(Offset.Zero) } // center od canvas


    /**
     * for smooth animation when a node is selected
     * if selected then node grow by 125% when released then go back to normal
     */
    val animationScale by animateFloatAsState(
        targetValue = if (selectedNodeId != null) 1.15f else 1f,
        label = "node_scale_animation"
    )

    /**
     * Main UI screen contains two major part
     * 1. Canvas to draw all node and edges i.e. graph
     * 2. Two(2) overlay buttons for zoom-in(+) and zoom-out(-)
     */
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /**
         * A custom surface where graphs are being rendered
         */
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                /**
                 * For zoom gesture
                 */
                .pointerInput(Unit) {
                    // zoom gesture
                    detectTransformGestures { _, pan, zoom, _ -> // to detect two finger touch
                        // two finger pan and pinch-zoom
                        scale = (scale * zoom).coerceIn(0.25f, 4f) // from 30% to 400% zoom
                        offset += pan / scale // to adjust the pan speed based on zoom level
                    }
                }

                /**
                 * for drag gesture
                  */
                .pointerInput(scale, offset, canvasCenter) {
                    //  drag gesture
                    awaitEachGesture {
                        val down = awaitFirstDown() // waits for finger down

                        // converts the touch position to graph position/space
                        val adjustedTouch = (down.position - offset - canvasCenter) / scale + canvasCenter

                        // to find which node was touched by checking if touch was is in any node radius
                        val touchedNodeId = nodePosition.entries.find { (_, pos) ->
                            adjustedTouch.getDistanceTo(pos) <= nodeRadius
                        }?.key

                        if (touchedNodeId != null) {
                            // marking the node as selected node
                            selectedNodeId = touchedNodeId

                            // to track the finger movement
                            drag(down.id) { change ->
                                val dragAmount = change.positionChange()
                                nodePosition[touchedNodeId]?.let { currentPosition ->
                                    nodePosition[touchedNodeId] = currentPosition + (dragAmount / scale)
                                }
                                change.consume() // marks the gesture event as handled to avoid conflict with other events
                            }
                            selectedNodeId = null // clear the selection after release of finger
                        }

                    }
                }
        ) {

            // calculate the center point and assign it to mutable variable i.e. canvasCenter
            val center = Offset(size.width / 2, size.height / 2)
            canvasCenter = center

            /**
             * Safety Check
             * If the graph data is not found then exit here
             */
            if (graphData.nodes.isEmpty()) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText("No data found", center.x, center.y, textPaint)
                }
            }

            /**
             * Hierarchical Layout Algorithm
             * create a map with key and value as all the child node eg. {A -> [B, C]}
             * create a inDegree to store how many node point to each node
             */

            if (nodePosition.isEmpty()) {
                // to build the adjacency list
                val adjacencyList = mutableMapOf<String, MutableList<String>>() // create a map like {A -> [B, C]}
                val inDegree = mutableMapOf<String, Int>() // to store count of how many node point to each node

                // add data to above two variables
                graphData.edges.forEach { edge ->
                    adjacencyList[edge.from]?.add(edge.to) // populate the adjancyList
                    inDegree[edge.to] = (inDegree[edge.to] ?: 0) + 1 // if there is no in-degree then 0 and add 1 to each finding
                }

                /**
                 * Finding the root node
                 * The root node should have no incoming node
                 * in case of cycle then use 1st node as root node
                 */
                val rootNode = graphData.nodes.filter { (inDegree[it.id] ?: 0) == 0 }
                val roots = if ( rootNode.isEmpty()) listOf(graphData.nodes.first()) else rootNode // fallback in case of cycle

                // Hierarchical layout parameters
                val startY = 180f // top margin for title
                val levelHeight = 380f // vertical space between levels
                val horizontalSpacing = 350f // horizontal space between sibling nodes


                /**
                 * BFS(Breadth First Search) leveling
                 * Working:
                 * 1. assign each node a level i.e. depth in the tree
                 *      level 0 : root
                 *      level 1 : children
                 *      level 2 : grandchildren and so on
                 */
                val levels = mutableMapOf<String, Int>()
                val queue = ArrayDeque<Pair<String, Int>>()
                val visited = mutableSetOf<String>()

                roots.forEach { root ->
                    queue.add(root.id to 0)
                    visited.add(root.id)
                }

                while (queue.isNotEmpty()){
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
                 * Positioning nodes
                 */
                val nodesByLevel = levels.entries.groupBy ({ it.value }, {it.key})

                nodesByLevel.forEach { (level, nodesInLevel) ->
                    val y = startY + level + levelHeight // vertical position
                    val levelWidth = (nodesInLevel.size - 1) * horizontalSpacing // total width of each level
                    val startX = center.x - levelWidth / 2 // place then in center horizontally

                    nodesInLevel.forEachIndexed { index, nodeId ->
                        val x = if (nodesInLevel.size == 1) {
                            center.x // if there is a single node then place it in center
                        } else {
                            startX + index + horizontalSpacing // to spread node evenly in each level
                        }
                        nodePosition[nodeId] = Offset(x, y)
                    }
                }
            }

            /**
             * Transform for zoom and pan
             */

            withTransform ({
                scale(scaleX = scale, scaleY = scale, pivot = center) // to scale around the center point
                translate(left = offset.x, top = offset.y) // to perform pan based on offset
            }) {

                /**
                 * Drawing the edges i.e. relationship between nodes
                 */
                graphData.edges.forEach { edge ->
                    val fromPos = nodePosition[edge.from]  // position of from node
                    val toPos = nodePosition[edge.to] // position of to node

                    if (fromPos != null && toPos != null) {
                        val path = Path().apply {
                            moveTo(fromPos.x, fromPos.y)
                            val controlX = (fromPos.x + toPos.x) / 2
                            val controlY = fromPos.y + (toPos.y - fromPos.y) * 0.3f
                            quadraticTo(controlX, controlY, toPos.x, toPos.y) // to create  a arc instead of a straight line
                        }
                         drawPath(
                             path,
                             color = Color(0xFF6B7280), // TextSecondaryColor
                             style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                         )
                        /**
                         * adding the label to edges
                         */
                        // position of the edge label
                        val midX = (fromPos.x + toPos.x) / 2 // middle of edge horizontally
                        val midY = (fromPos.y + toPos.y) / 2 - 30 // slightly above edge

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(edge.label, midX, midY, edgeLabelPaint)
                        }
                    }
                }

                /**
                 * Drawing the nodes into the canvas
                 */
                graphData.nodes.forEach { node ->
                    val pos = nodePosition[node.id] ?: return@forEach

                    val color = if (selectedNodeId == node.id) {
                        colorForCategory(node.category).copy(alpha = 0.9f)  // Highlight
                    } else {
                        colorForCategory(node.category)
                    }
                    val radius = if (selectedNodeId == node.id) {
                        nodeRadius * animationScale
                    } else {
                        nodeRadius
                    }

                    // drop shadow
                    drawCircle(
                        color = Color.LightGray,
                        radius = radius,
                        center = pos + Offset(4f, 4f) // offset for depth
                    )
                    // main node
                    drawCircle(color, radius, center = pos)

                    //inner highlight circle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f), // for a little shine
                        radius = radius * 0.85f,
                        center = pos
                    )
                    // Node label
                    drawMultiLineText(
                        text = node.label,
                        center = pos,
                        textPaint = textPaint,
                        maxWidth = radius * 1.6f
                    )
                }
            }

            /**
             * Draw the title
             * not affected by zoom or pan
             */
            drawIntoCanvas { canvas ->
                val titleY = 60f
                canvas.nativeCanvas.drawText(graphData.main_concept, center.x, titleY, titlePaint)
            }


        }

        /**
         * Column for two buttons used for zoom-in(+) and zoom-out(-)
         */Unit
        Column(
            modifier = Modifier
                .align ( Alignment.TopEnd )
                .padding(16.dp)
        ) {
            // zoom in button (+)
            FloatingActionButton(
                onClick = {
                    scale = (scale * 1.25f).coerceIn(0.25f, 4f)
                    Log.d(TAG, "Zoom In button clicked")
                } ,
                modifier = Modifier.size(37.dp),
                shape = RectangleShape,
                containerColor = textFieldBackgroundColor,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom in Icon",
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            // zoom out button (-)
            FloatingActionButton(
                onClick = {
                    scale = (scale / 1.25f).coerceIn(0.25f, 4f)
                    Log.d(TAG, "Zoom Out button clicked")
                } ,
                modifier = Modifier.size(37.dp),
                shape = RectangleShape,
                containerColor = textFieldBackgroundColor,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom in Icon",
                    tint = TextSecondary
                )
            }
        }
    }
}

/**
 * Split text into words
 * Add words to line until it exceeds maxWidth
 * Start new line when overflow happens
 * Draw each line with proper vertical spacing
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

    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (textPaint.measureText(testLine) > maxWidth) {
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
            currentLine = word
        }
        else {
            currentLine = testLine
        }
    }
}

private fun Offset.getDistanceTo(other: Offset): Float =
    hypot(x - other.x, y - other.y)

/**
 * Alternative method but less precise
 * fun Offset.getDistanceTo(other: Offset): Float {
 *     val dx = x - other.x
 *     val dy = y - other.y
 *     return sqrt(dx * dx + dy * dy)
 * }
 */

