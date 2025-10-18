package com.ragnar.eduapp.core.chatBot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ragnar.eduapp.core.chatBot.ChatViewModel

class ChatViewModelFactory(
    private val apiKey: String,
    private val userClass : String,
    private val nodeNumber: String,
    private val maxWord: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(apiKey, userClass, nodeNumber, maxWord) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
