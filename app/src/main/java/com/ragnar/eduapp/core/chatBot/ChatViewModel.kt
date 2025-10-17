package com.ragnar.eduapp.core.chatBot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragnar.eduapp.data.model.ChatMessageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.plus

class ChatViewModel(
    apiKey: String
) : ViewModel() {
    private val llmClient = GroqLLMClient(apiKey)


    // a list to store recent chat for the current session
    private val _messages = MutableStateFlow<List<ChatMessageModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageModel>> = _messages


    // loading state managers
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // for PopUp simulation screen
    private var messageCount = 0
    private val _showPopup = MutableStateFlow(false)
    val showPopup: StateFlow<Boolean> = _showPopup


    // default JSON in case of failure
    private val _conceptMapJSON = MutableStateFlow("""{"visualization_type":"None","main_concept":"Chat for a Concept Map","nodes":[],"edges":[]}""")

    val conceptMapJSON: StateFlow<String> = _conceptMapJSON

    // local variable to store user message in case of any error
    private var lastUserMessage: String = ""

    fun sendMessage(userMessage: String) {
        // Add user message to chat
        val userMsg = ChatMessageModel(content = userMessage, sender = "user")
        _messages.value += userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Make a single API call
                val response = llmClient.queryLLM(userMessage)

                // Extract both the answer and concept map from the same response
                val answerText = llmClient.extractAnswer(response)
                val conceptMapJson = llmClient.extractConceptMapJSON(response)

                // Update chat with answer
                _messages.value += ChatMessageModel(
                    content = answerText,
                    sender = "ai"
                )

                // Update concept map
                _conceptMapJSON.value = conceptMapJson

                Log.i("ChatViewModel", "Success: Answer and concept map updated: $conceptMapJson")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message: ${e.message}", e)
                _messages.value += ChatMessageModel(
                    content = "Sorry, I encountered an error: ${e.message}",
                    sender = "ai",
                    canRetry = true
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // method to retry if there is any error
    fun retryLastMessage() {
        if (lastUserMessage.isNotBlank()) {
            _messages.value = _messages.value.filter { !(it.sender == "ai" && it.isError) }
            sendMessage(lastUserMessage)
        }
    }

//    fun dismissPopUp() {
//        _showPopup.value = false
//    }
}