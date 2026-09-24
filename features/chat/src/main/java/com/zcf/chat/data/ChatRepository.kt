package com.zcf.chat.data

import com.zcf.chat.di.DeepSeekApiKey
import com.zcf.chat.domain.ChatMessage
import com.zcf.chat.domain.ChatModel
import com.zcf.chat.domain.ChatRole
import com.zcf.network.deepseek.ChatCompletionRequest
import com.zcf.network.deepseek.ChatError
import com.zcf.network.deepseek.ChatMessageDto
import com.zcf.network.deepseek.ChatStreamEvent
import com.zcf.network.deepseek.DeepSeekClient
import com.zcf.network.deepseek.ThinkingConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class ChatRepository @Inject constructor(
    private val client: DeepSeekClient,
    @DeepSeekApiKey private val apiKey: String
) {
    val isConfigured: Boolean get() = apiKey.isNotBlank()

    /**
     * Streams a reply for [messages]. Short-circuits to a local failure when no key is configured,
     * so a misconfigured build reports that plainly instead of issuing a request that can only
     * come back as an opaque 401.
     */
    fun stream(messages: List<ChatMessage>, model: ChatModel, thinkingEnabled: Boolean): Flow<ChatStreamEvent> {
        if (!isConfigured) return flowOf(ChatStreamEvent.Failed(ChatError.MissingApiKey))

        val request = ChatCompletionRequest(
            model = model.id,
            messages = messages.map { it.toWireMessage() },
            stream = true,
            thinking = ThinkingConfig(
                if (thinkingEnabled) ThinkingConfig.ENABLED else ThinkingConfig.DISABLED
            )
        )
        return client.streamChat(request)
    }
}

/**
 * Only role and content travel back to the API. `reasoning_content` is output-only, and the
 * assistant placeholder currently being streamed into is not part of the history yet.
 */
private fun ChatMessage.toWireMessage(): ChatMessageDto = ChatMessageDto(
    role = when (role) {
        ChatRole.USER -> "user"
        ChatRole.ASSISTANT -> "assistant"
    },
    content = content
)
