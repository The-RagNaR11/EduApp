package com.ragnar.eduapp.core.chatBot

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/*
    Utility class for handling AI chat requests using Groq API
 */
class AIChatUtils(private val apiKey: String) {

    private val client = OkHttpClient()

    /*
        Send a message to the Groq model and get response.
     */
    suspend fun sendMessage(message: String): String = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.groq.com/openai/v1/chat/completions"

            // Create messages array
            val messagesArray = JSONArray()
            val messageObj = JSONObject().apply {
                put("role", "user")
                put("content", message)
            }
            messagesArray.put(messageObj)

            val json = JSONObject().apply {
                put("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                put("messages", messagesArray)
                put("max_tokens", 1024)
                put("temperature", 0.7)
            }

            val body = json.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())

            Log.d("AIChatUtils", "Sending request to: $url")
            Log.d("AIChatUtils", "Request body: $json")

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request)
                .execute()
                .use { response ->
                    val responseBody = response.body?.string()

                    Log.d("AIChatUtils", "Response code: ${response.code}")
                    Log.d("AIChatUtils", "Response body: $responseBody")

                    if (!response.isSuccessful) {
                        return@withContext when (response.code) {
                            401 -> "Error 401: Invalid API key. Please check your Groq API key."
                            429 -> "Error 429: Rate limit exceeded. Please try again later."
                            500 -> "Error 500: Server error. Please try again later."
                            else -> "Error ${response.code}: ${response.message}"
                        }
                    }

                    if (responseBody.isNullOrEmpty()) {
                        return@withContext "Error: Empty response from server"
                    }

                    val responseJson = JSONObject(responseBody)

                    // Check if response has error
                    if (responseJson.has("error")) {
                        val errorObj = responseJson.getJSONObject("error")
                        val errorMessage = errorObj.getString("message")
                        return@withContext "API Error: $errorMessage"
                    }

                    // Extract the content
                    val choicesArray = responseJson.getJSONArray("choices")
                    if (choicesArray.length() == 0) {
                        return@withContext "Error: No response choices available"
                    }

                    val firstChoice = choicesArray.getJSONObject(0)
                    val messageObj = firstChoice.getJSONObject("message")
                    val content = messageObj.getString("content")

                    return@withContext content.trim()
            }
        } catch (e: Exception) {
            Log.e("AIChatUtils", "Exception in sendMessage", e)
            return@withContext "Network Error: ${e.message}"
        }
    }
}