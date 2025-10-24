package com.ragnar.eduapp.data.dataClass

import android.R
import kotlinx.serialization.Serializable

@Serializable
data class GraphData(
    val visualization_type: String,
    val main_concept: String,
    val nodes: List<Node>,
    val edges: List<Edge>,
    val audioSegments: List<AudioSegment> = emptyList()
) {
    @Serializable
    data class Node(
        val id: String,
        val label: String,
        val category: String
    )

    @Serializable
    data class Edge(
        val from: String,
        val to: String,
        val label: String,
        val id: String = "$from->$to"
    )

    @Serializable
    data class AudioSegment(
        val startTime: Float,
        val endTime: Float,
        val spokenText: String,
        val estimatedDuration: Float,
        val highlightNodeId: List<String> = emptyList(),
        val highlightEdgeIds : List<String> = emptyList(),
        val showNodeIds: List<String> = emptyList(), // nodes to revel
        val action: String = "introduce" // "introduce", "expand", "connect"
    )
}