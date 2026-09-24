package com.zcf.network.deepseek

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of `POST /chat/completions`.
 *
 * [stream] deliberately has no default value: the encoder is configured with
 * `encodeDefaults = true`, but a default here would still be the kind of thing that silently
 * flips the API into non-streaming mode if that setting is ever changed.
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val stream: Boolean,
    val thinking: ThinkingConfig? = null
)

/**
 * A message as the API accepts it. Reasoning is output-only: `reasoning_content` must never be
 * sent back, so it lives on the UI model in the feature module rather than here.
 */
@Serializable
data class ChatMessageDto(val role: String, val content: String)

@Serializable
data class ThinkingConfig(val type: String) {
    companion object {
        const val ENABLED: String = "enabled"
        const val DISABLED: String = "disabled"
    }
}

/** One `chat.completion.chunk` event. `choices` is empty on the final usage-carrying chunk. */
@Serializable
internal data class ChatCompletionChunk(val choices: List<ChunkChoice> = emptyList(), val usage: UsageDto? = null)

@Serializable
internal data class ChunkChoice(
    val delta: ChunkDelta? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
internal data class ChunkDelta(
    val content: String? = null,
    @SerialName("reasoning_content") val reasoningContent: String? = null
)

/** Shape the API uses for non-2xx responses. */
@Serializable
internal data class ApiErrorEnvelope(val error: ApiErrorBody? = null)

@Serializable
internal data class ApiErrorBody(val message: String? = null, val type: String? = null)

@Serializable
internal data class UsageDto(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0
)
