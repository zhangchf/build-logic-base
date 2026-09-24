package com.zcf.network.deepseek

/**
 * One event read off a DeepSeek chat stream. Failures are modelled as a terminal [Failed] event
 * rather than a thrown exception, so [DeepSeekClient.streamChat] never throws except on
 * cancellation; callers get a single, uniform shape to fold into their state.
 */
sealed interface ChatStreamEvent {
    /** A chunk of the model's private reasoning. Only arrives while thinking is enabled. */
    data class Reasoning(val text: String) : ChatStreamEvent

    /** A chunk of the answer shown to the user. */
    data class Content(val text: String) : ChatStreamEvent

    /** Token accounting, which the API rides on the final chunk. */
    data class Usage(val promptTokens: Int, val completionTokens: Int) : ChatStreamEvent

    /** The stream ended cleanly. [reason] is the API's `finish_reason`, null if it never sent one. */
    data class Finished(val reason: String?) : ChatStreamEvent

    /** The stream ended badly. Always the last event emitted. */
    data class Failed(val error: ChatError) : ChatStreamEvent
}

sealed interface ChatError {
    /** A non-2xx response. [detail] is the API's own error message when it sent one. */
    data class Http(val code: Int, val detail: String?) : ChatError

    /** The request never completed: DNS, TLS, timeout, or a dropped connection. */
    data class Transport(val message: String?) : ChatError

    /** A 2xx response that nevertheless carried no body. */
    data object EmptyResponse : ChatError

    /** A chunk arrived that the model could not be decoded from. */
    data object MalformedChunk : ChatError

    /** No API key was configured, so no request was attempted. */
    data object MissingApiKey : ChatError
}
