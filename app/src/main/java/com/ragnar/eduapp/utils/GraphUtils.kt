package com.ragnar.eduapp.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ragnar.eduapp.data.dataClass.GraphData
import kotlinx.serialization.json.Json

class GraphUtils {
    fun createDefaultGraphData(): GraphData {
        val defaultJson = """
    {
      "visualization_type": "Concept Map",
      "main_concept": "Loading...",
      "nodes": [
        {"id": "A", "label": "Concept", "category": "Core"}
      ],
      "edges": [],
      "audioSegments": [
        {
          "segmentIndex": 0,
          "spokenText": "Loading concept map...",
          "estimatedDuration": 2.0,
          "highlightNodeIds": ["A"],
          "showNodeIds": ["A"],
          "highlightEdgeIds": [],
          "action": "introduce"
        }
      ]
    }
    """.trimIndent()

        try {
            return Json.decodeFromString<GraphData>(defaultJson)
        } catch (e: Exception) {
            throw IllegalStateException("Graph class structure mismatch:\n $e")
        }
    }
}