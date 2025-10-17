package com.ragnar.eduapp.data.dataClass

import kotlinx.serialization.Serializable

@Serializable
data class GraphData(
    val visualization_type: String,
    val main_concept: String,
    val nodes: List<Node>,
    val edges: List<Edge>
)