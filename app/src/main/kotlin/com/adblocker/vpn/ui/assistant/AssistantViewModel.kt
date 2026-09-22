package com.adblocker.vpn.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

data class ChatMessage(val id: String = UUID.randomUUID().toString(), val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

class AssistantViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(text = "Hello! I am your Cyber Assistant. How can I help you today? Try saying 'Wolt is blocked' or 'Block YouTube'.", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun processUserMessage(text: String, onAddWhitelist: (String) -> Unit, onAddBlacklist: (String) -> Unit, onPanicMode: () -> Unit) {
        val userMsg = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMsg
        
        viewModelScope.launch {
            _isTyping.value = true
            delay(1200) // simulate AI thinking delay
            
            val lowerText = text.lowercase()
            
            var responseText = "I didn't quite catch that. Try asking me to unblock an app or activate panic mode!"
            
            // Simple NLP Intent Matching
            if (lowerText.contains("wolt") && (lowerText.contains("נחסם") || lowerText.contains("unblock") || lowerText.contains("שחרר"))) {
                onAddWhitelist("wolt.com")
                onAddWhitelist("woltapi.com")
                onAddWhitelist("wolt.co.il")
                responseText = "I found that Wolt was being blocked. I have whitelisted 'wolt.com', 'woltapi.com', and 'wolt.co.il'. You can order food now! 🍔"
            } else if (lowerText.contains("block") || lowerText.contains("חסום") || lowerText.contains("תחסום")) {
                val target = lowerText.replace("block", "").replace("תחסום את", "").replace("חסום את", "").trim()
                if (target.isNotBlank()) {
                    val domain = if (target.contains(".")) target else "$target.com"
                    onAddBlacklist(domain)
                    responseText = "I have added '$domain' to your Blacklist. It will be blocked immediately! 🛡️"
                } else {
                    responseText = "What do you want me to block?"
                }
            } else if (lowerText.contains("panic") || lowerText.contains("פאניקה")) {
                onPanicMode()
                responseText = "🚨 PANIC MODE ACTIVATED. All non-essential traffic is now blocked."
            } else if (lowerText.contains("unblock") || lowerText.contains("שחרר")) {
                val target = lowerText.replace("unblock", "").replace("שחרר את", "").trim()
                if (target.isNotBlank()) {
                    val domain = if (target.contains(".")) target else "$target.com"
                    onAddWhitelist(domain)
                    responseText = "I have whitelisted '$domain'. It should work now! ✅"
                } else {
                    responseText = "What do you want me to unblock?"
                }
            }
            
            _messages.value = _messages.value + ChatMessage(text = responseText, isUser = false)
            _isTyping.value = false
        }
    }
}
