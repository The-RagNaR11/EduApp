package com.ragnar.eduapp.core.chatBot

import android.util.Log
import com.ragnar.eduapp.utils.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GroqLLMClient(
    private val apiKey: String,
    private val userClass: String,
    private val nodeNumber: String,
    private val maxWord: String

) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val url = "https://api.groq.com/openai/v1/chat/completions"
    /**
     * Main query method that returns the complete API response
     */
    suspend fun queryLLM(userQuery: String): String = withContext(Dispatchers.IO) {
        try {
            // System + user prompt messages
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", """
                    You are an intelligent and structured AI tutor for a Class $userClass student.
                    You must respond strictly in the following format and hierarchy — no markdown, no extra text, and no explanations outside this structure.
                
                    [ANSWER]
                    Provide a clear, natural, and age-appropriate explanation for the student's query in less than $maxWord words.
                    Keep the tone educational and concise.
                    Write in a way that flows naturally when spoken aloud.
                
                    [CONCEPT_MAP_JSON]
                    Provide ONLY a valid JSON object describing a hierarchical concept map with audio synchronization data.
                    Use this exact structure:
                    {
                      "visualization_type": "Concept Map",
                      "main_concept": "Main idea or topic",
                      "nodes": [
                        {"id": "A", "label": "Main Concept", "category": "Main"},
                        {"id": "B", "label": "Sub Concept 1", "category": "Secondary"},
                        {"id": "C", "label": "Sub Concept 2", "category": "Secondary"},
                        {"id": "D", "label": "Leaf Concept 1", "category": "Leaf"}
                      ],
                      "edges": [
                        {"from": "A", "to": "B", "label": "relation", "id": "A->B"},
                        {"from": "A", "to": "C", "label": "relation", "id": "A->C"},
                        {"from": "B", "to": "D", "label": "relation", "id": "B->D"}
                      ],
                      "audioSegments": [
                        {
                          "segmentIndex": 0,
                          "spokenText": "excerpt from answer that mentions concept",
                          "estimatedDuration": 3.5,
                          "highlightNodeIds": ["A"],
                          "showNodeIds": ["A"],
                          "highlightEdgeIds": [],
                          "action": "introduce"
                        },
                        {
                          "segmentIndex": 1,
                          "spokenText": "next part of answer",
                          "estimatedDuration": 4.2,
                          "highlightNodeIds": ["B", "C"],
                          "showNodeIds": ["B", "C"],
                          "highlightEdgeIds": ["A->B", "A->C"],
                          "action": "expand"
                        }
                      ]
                    }
                
                    Hierarchy Rules:
                    - There must be exactly **1 Main node** (root concept).
                    - Include **2–3 Secondary nodes** connected directly to the main node.
                    - Remaining nodes should be **Leaf nodes**, connected to secondary nodes.
                    - Include approximately $nodeNumber total nodes.
                    - All nodes and edges must form a clear hierarchical structure.
                    
                    Audio Synchronization Rules:
                    - Each edge MUST have a unique "id" field in format "FROM_ID->TO_ID"
                    - Create audioSegments array that breaks down the [ANSWER] into logical parts
                    - Each segment should:
                      * Include the actual text excerpt from your answer
                      * Estimate duration in seconds (average speaking pace: 2-3 words per second)
                      * Specify which node IDs to highlight during this segment
                      * Specify which node IDs to reveal/show (previously hidden nodes)
                      * Specify which edge IDs to highlight (use the edge "id" field)
                      * Include an action: "introduce" (first mention), "expand" (add details), "connect" (show relationships)
                    - Segments should be ordered sequentially (segmentIndex 0, 1, 2...)
                    - The sum of all estimatedDuration values should roughly match the length of your [ANSWER]
                    - Initially only show the main concept (root node), reveal others progressively
                    
                    Example flow:
                    1. Segment 0: Introduce main concept → show node A
                    2. Segment 1: Explain first branch → show nodes B, C and edge A->B, A->C
                    3. Segment 2: Add details → show nodes D, E and edges B->D, C->E
                    
                    JSON must be valid, syntactically correct, and directly parsable (no markdown or code block).
                    Do not add any explanations, comments, or text after the JSON.
                    """.trimIndent()
                    )
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userQuery)
                })
            }

            val jsonBody = JSONObject().apply {
                put("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                put("messages", messages)
                put("temperature", 0.7)
                put("max_tokens", 1024)
            }

            DebugLogger.debugLog("AIChatUtils", "Sending request to: $url")
            DebugLogger.debugLog("AIChatUtils", "Request body: $jsonBody")

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            DebugLogger.debugLog("AIChatUtils", "Response code: ${response.code}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                DebugLogger.errorLog("AIChatUtils", "Error: ${response.code} - ${response.message}")
                DebugLogger.errorLog("AIChatUtils", "Error body: $errorBody")
                response.close()
                return@withContext "Error: ${response.code} - ${response.message}"
            }

            // Read response body
            val responseBody = response.body?.string() ?: ""
            response.close()

            DebugLogger.debugLog("AIChatUtils", "Response body: $responseBody")
            return@withContext responseBody

        } catch (e: Exception) {
            DebugLogger.errorLog("AIChatUtils", "Exception during API call: ${e.message}")
            return@withContext "Error: ${e.message}"
        }
    }

    /**
     * Extracts the concept map JSON from the AI response
     */
    fun extractConceptMapJSON(fullResponse: String): String {
        try {
            // Parse the full API response
            val jsonResponse = JSONObject(fullResponse)
            val choices = jsonResponse.getJSONArray("choices")

            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")

                DebugLogger.debugLog("AIChatUtils", "Extracting concept map from content of length: ${content.length}")

                // Look for any JSON object that contains "visualization_type": "Concept Map"
                var currentPos = 0

                while (currentPos < content.length) {
                    val startPos = content.indexOf("{", currentPos)
                    if (startPos == -1) break

                    // Find the matching closing brace
                    var braceCount = 0
                    var endIndex = startPos

                    for (i in startPos until content.length) {
                        when (content[i]) {
                            '{' -> braceCount++
                            '}' -> {
                                braceCount--
                                if (braceCount == 0) {
                                    endIndex = i + 1
                                    break
                                }
                            }
                        }
                    }

                    if (endIndex > startPos) {
                        // Try to parse this JSON object
                        val candidateJson = content.substring(startPos, endIndex)

                        try {
                            val testObj = JSONObject(candidateJson)
                            // Check if it's a concept map JSON
                            if (testObj.has("visualization_type") &&
                                testObj.has("main_concept") &&
                                testObj.has("nodes") &&
                                testObj.has("edges")) {

                                DebugLogger.debugLog("AIChatUtils", "Successfully extracted concept map JSON: $candidateJson")

                                // Post-process: Add edge IDs if missing
                                val processedJson = addEdgeIdsIfMissing(candidateJson)

                                return processedJson
                            }
                        } catch (e: Exception) {
                            // Not a valid JSON or not a concept map, continue searching
                            DebugLogger.errorLog("GroqLLMClient", "Not a valid JSON or not a concept map, continue searching: ${e.message}")
                        }
                    }

                    currentPos = endIndex
                }
            }

            DebugLogger.errorLog("AIChatUtils", "Could not extract concept map JSON from response")
            return getDefaultConceptMapJSON()

        } catch (e: Exception) {
            DebugLogger.errorLog("AIChatUtils", "Error extracting concept map JSON: ${e.message}")
            return getDefaultConceptMapJSON()
        }
    }


    /**
     * Adds "id" field to edges if missing
     * Format: "FROM_ID->TO_ID"
     */
    private fun addEdgeIdsIfMissing(jsonString: String): String {
        try {
            val jsonObject = JSONObject(jsonString)
            val edges = jsonObject.getJSONArray("edges")

            var modified = false
            for (i in 0 until edges.length()) {
                val edge = edges.getJSONObject(i)

                // Add ID if missing
                if (!edge.has("id") || edge.getString("id").isBlank()) {
                    val from = edge.getString("from")
                    val to = edge.getString("to")
                    edge.put("id", "$from->$to")
                    modified = true
                }
            }

            if (modified) {
                DebugLogger.debugLog("AIChatUtils", "Added missing edge IDs")
            }

            return jsonObject.toString()

        } catch (e: Exception) {
            DebugLogger.errorLog("AIChatUtils", "Error adding edge IDs: ${e.message}")
            return jsonString // Return original if processing fails
        }
    }


    /**
     * Extracts the answer text from the AI response
     */
    fun extractAnswer(fullResponse: String): String {
        try {
            val jsonResponse = JSONObject(fullResponse)
            val choices = jsonResponse.getJSONArray("choices")

            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")

                // Try to find [ANSWER] marker
                val answerStart = content.indexOf("[ANSWER]")
                if (answerStart != -1) {
                    val afterAnswer = content.substring(answerStart + "[ANSWER]".length).trim()

                    // Find where the JSON starts (to exclude it)
                    val jsonStart = afterAnswer.indexOf("{")

                    // Also find [CONCEPT_MAP_JSON] marker as alternative boundary
                    val conceptMapMarker = afterAnswer.indexOf("[CONCEPT_MAP_JSON]")

                    // Determine where to cut the answer
                    val cutPosition = when {
                        conceptMapMarker != -1 -> conceptMapMarker
                        jsonStart != -1 -> jsonStart
                        else -> afterAnswer.length
                    }

                    val rawAnswer = if (cutPosition != afterAnswer.length) {
                        afterAnswer.substring(0, cutPosition).trim()
                    } else {
                        afterAnswer
                    }

                    // Clean up the answer by removing any remaining bracket markers
                    var cleanedAnswer = rawAnswer

                    // Remove patterns like [COW], [Concept Map], [ANYTHING] at the end or in middle
                    cleanedAnswer = cleanedAnswer.replace(Regex("\\[.*?\\]"), "").trim()

                    // Remove any trailing markers or labels before returning
                    return cleanedAnswer.trim()
                }

                // No [ANSWER] marker, try to extract text before JSON or markers
                var rawContent = content

                // Find first JSON occurrence
                val firstBrace = rawContent.indexOf("{")
                if (firstBrace > 0) {
                    rawContent = rawContent.substring(0, firstBrace).trim()
                }

                // Find [CONCEPT_MAP_JSON] marker
                val conceptMapMarker = rawContent.indexOf("[CONCEPT_MAP_JSON]")
                if (conceptMapMarker != -1) {
                    rawContent = rawContent.substring(0, conceptMapMarker).trim()
                }

                // Clean up any bracket markers
                rawContent = rawContent.replace(Regex("\\[.*?\\]"), "").trim()

                return if (rawContent.isNotBlank()) rawContent else content.trim()
            }

            return "I encountered an error processing the response."

        } catch (e: Exception) {
            DebugLogger.errorLog("AIChatUtils", "Error extracting answer: ${e.message}")
            return "I encountered an error processing the response."
        }
    }

    /**
     * Returns a default concept map JSON when extraction fails
     */
    private fun getDefaultConceptMapJSON(): String {
        return """
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
    }
}