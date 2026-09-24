package com.zcf.network.deepseek

import kotlinx.serialization.json.Json
import okio.BufferedSource

/**
 * Reads a `text/event-stream` body to exhaustion, handing each decoded event to [emit].
 *
 * Returns null when the stream ended cleanly, or the error that ended it. Payloads that fail to
 * decode are skipped rather than aborting the stream, so an unrecognised event type from the API
 * cannot kill an otherwise healthy reply. A stream that yields nothing at all is reported as
 * malformed, which is what a non-SSE body (a captive portal, say) looks like from here.
 */
internal suspend fun readChatStream(
    source: BufferedSource,
    json: Json,
    emit: suspend (ChatStreamEvent) -> Unit
): ChatError? {
    var decodedChunks = 0
    var reading = true

    while (reading) {
        val sseLine = source.readUtf8Line()?.let(::parseSseLine)
        reading = sseLine != null && sseLine !is SseLine.Done
        if (sseLine is SseLine.Data && emitChunk(sseLine.payload, json, emit)) {
            decodedChunks++
        }
    }

    return if (decodedChunks == 0) ChatError.MalformedChunk else null
}

/** Decodes one payload and emits the events it carries, reporting whether it decoded at all. */
private suspend fun emitChunk(payload: String, json: Json, emit: suspend (ChatStreamEvent) -> Unit): Boolean {
    val chunk = decodeChunk(json, payload) ?: return false
    chunk.toEvents().forEach { emit(it) }
    return true
}

internal fun decodeChunk(json: Json, payload: String): ChatCompletionChunk? =
    runCatching { json.decodeFromString<ChatCompletionChunk>(payload) }.getOrNull()

/** Pulls the API's own `error.message` out of an error body, when it sent a recognisable one. */
internal fun decodeErrorMessage(json: Json, raw: String): String? =
    runCatching { json.decodeFromString<ApiErrorEnvelope>(raw).error?.message }.getOrNull()
