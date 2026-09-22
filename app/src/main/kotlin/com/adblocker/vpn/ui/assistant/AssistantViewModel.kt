package com.adblocker.vpn.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

data class ChatMessage(val id: String = UUID.randomUUID().toString(), val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

data class AppTarget(
    val id: String,
    val domains: List<String>,
    val packages: List<String>
)

val knownTargets = listOf(
    AppTarget("wolt", listOf("wolt.com", "woltapi.com", "wolt.co.il"), listOf("com.wolt.android")),
    AppTarget("youtube", listOf("youtube.com", "googlevideo.com", "ytimg.com"), listOf("com.google.android.youtube")),
    AppTarget("facebook", listOf("facebook.com", "fbcdn.net"), listOf("com.facebook.katana")),
    AppTarget("instagram", listOf("instagram.com", "cdninstagram.com"), listOf("com.instagram.android")),
    AppTarget("spotify", listOf("spotify.com"), listOf("com.spotify.music")),
    AppTarget("tiktok", listOf("tiktok.com", "tiktokv.com", "byteoversea.com"), listOf("com.ss.android.ugc.trill", "com.zhiliaoapp.musically")),
    AppTarget("whatsapp", listOf("whatsapp.com", "whatsapp.net", "cdn.whatsapp.net"), listOf("com.whatsapp")),
    AppTarget("aliexpress", listOf("aliexpress.com", "alicdn.com", "alibaba.com"), listOf("com.alibaba.aliexpresshd"))
)

class AssistantViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(text = "Hello! I am your Cyber Assistant. How can I help you today? Try saying 'Wolt is blocked' or 'Block YouTube'.", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun processUserMessage(
        text: String, 
        onAddWhitelist: (String) -> Unit, 
        onAddBlacklist: (String) -> Unit, 
        onAddBypassedApp: (String) -> Unit,
        onRemoveBypassedApp: (String) -> Unit,
        onPanicMode: () -> Unit
    ) {
        val userMsg = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMsg
        
        viewModelScope.launch {
            _isTyping.value = true
            delay(1200) // simulate AI thinking delay
            
            val lowerText = text.lowercase()
            var responseText = "I didn't quite catch that. Try asking me to unblock an app or activate panic mode!"
            
            val targetMatch = knownTargets.find { lowerText.contains(it.id) }
            
            if (targetMatch != null) {
                if (lowerText.contains("נחסם") || lowerText.contains("unblock") || lowerText.contains("שחרר")) {
                    targetMatch.domains.forEach { onAddWhitelist(it) }
                    targetMatch.packages.forEach { onAddBypassedApp(it) }
                    responseText = "I identified '${targetMatch.id}' as both an app and a website. I have whitelisted its domains and bypassed its app traffic from the VPN. It should work perfectly now! ✅"
                } else if (lowerText.contains("block") || lowerText.contains("חסום") || lowerText.contains("תחסום")) {
                    targetMatch.domains.forEach { onAddBlacklist(it) }
                    targetMatch.packages.forEach { onRemoveBypassedApp(it) }
                    responseText = "I identified '${targetMatch.id}'. I have added its domains to your Blacklist and ensured its app is routed through the VPN for blocking! 🛡️"
                } else {
                    responseText = "I see you mentioned '${targetMatch.id}'. Do you want me to block or unblock it?"
                }
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
