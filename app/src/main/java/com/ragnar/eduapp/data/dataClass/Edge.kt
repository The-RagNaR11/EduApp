package com.ragnar.eduapp.data.dataClass

import kotlinx.serialization.Serializable

@Serializable
data class Edge (
    val from : String,
    val to : String,
    val label: String,
)