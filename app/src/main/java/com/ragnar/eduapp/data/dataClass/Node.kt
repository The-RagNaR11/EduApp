package com.ragnar.eduapp.data.dataClass

import kotlinx.serialization.Serializable

@Serializable
data class Node (
    val id : String,
    val label: String,
    val category: String
)