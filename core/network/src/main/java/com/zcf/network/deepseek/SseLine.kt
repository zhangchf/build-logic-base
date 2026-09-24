package com.zcf.network.deepseek

/** What a single line of the `text/event-stream` response means. */
internal sealed interface SseLine {
    /** A `data: <json>` payload. */
    data class Data(val payload: String) : SseLine

    /** The `data: [DONE]` sentinel that terminates the stream. */
    data object Done : SseLine

    /** A blank separator line, or a `:`-prefixed keep-alive comment. */
    data object Ignored : SseLine
}

private const val DATA_PREFIX = "data:"
private const val DONE_PAYLOAD = "[DONE]"

/**
 * Classifies one line of the response. DeepSeek sends single-line JSON payloads, so there is no
 * need to accumulate multi-line `data:` fields as the SSE spec allows.
 */
internal fun parseSseLine(line: String): SseLine {
    val trimmed = line.trim()
    val payload = trimmed.removePrefix(DATA_PREFIX).trim()
    return when {
        trimmed.isEmpty() -> SseLine.Ignored
        trimmed.startsWith(":") -> SseLine.Ignored
        !trimmed.startsWith(DATA_PREFIX) -> SseLine.Ignored
        payload == DONE_PAYLOAD -> SseLine.Done
        else -> SseLine.Data(payload)
    }
}

/** Flattens a decoded chunk into the events it carries, in the order they should be applied. */
internal fun ChatCompletionChunk.toEvents(): List<ChatStreamEvent> {
    val events = mutableListOf<ChatStreamEvent>()

    val choice = choices.firstOrNull()
    choice?.delta?.reasoningContent?.takeIf { it.isNotEmpty() }?.let {
        events += ChatStreamEvent.Reasoning(it)
    }
    choice?.delta?.content?.takeIf { it.isNotEmpty() }?.let {
        events += ChatStreamEvent.Content(it)
    }
    usage?.let {
        events += ChatStreamEvent.Usage(it.promptTokens, it.completionTokens)
    }
    if (choice?.finishReason != null) {
        events += ChatStreamEvent.Finished(choice.finishReason)
    }

    return events
}
