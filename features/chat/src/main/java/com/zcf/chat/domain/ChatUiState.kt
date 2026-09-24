package com.zcf.chat.domain

import androidx.compose.runtime.Immutable

@Immutable
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val model: ChatModel = ChatModel.FLASH,
    val thinkingEnabled: Boolean = true,
    val isStreaming: Boolean = false,
    val apiKeyConfigured: Boolean = false
) {
    val canSend: Boolean get() = input.isNotBlank() && !isStreaming && apiKeyConfigured
}
