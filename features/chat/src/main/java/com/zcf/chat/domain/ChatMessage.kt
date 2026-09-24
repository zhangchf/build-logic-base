package com.zcf.chat.domain

import androidx.compose.runtime.Immutable
import com.zcf.network.deepseek.ChatError

/**
 * One turn in the transcript.
 *
 * [reasoningExpanded] lives here rather than in a `remember` inside the list item, because
 * `LazyColumn` disposes off-screen items and would lose the state on scroll.
 */
@Immutable
data class ChatMessage(
    val id: Long,
    val role: ChatRole,
    val content: String = "",
    val reasoning: String = "",
    val reasoningExpanded: Boolean = true,
    val isStreaming: Boolean = false,
    val error: ChatError? = null,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null
)
