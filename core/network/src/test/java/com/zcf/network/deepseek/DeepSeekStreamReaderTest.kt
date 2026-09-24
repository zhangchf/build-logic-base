package com.zcf.network.deepseek

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepSeekStreamReaderTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private suspend fun read(body: String): Pair<List<ChatStreamEvent>, ChatError?> {
        val events = mutableListOf<ChatStreamEvent>()
        val error = readChatStream(Buffer().writeUtf8(body), json) { events += it }
        return events to error
    }

    private fun sse(vararg lines: String): String = lines.joinToString(separator = "\n", postfix = "\n")

    @Test
    fun emitsReasoningContentUsageAndFinishReasonInOrder() = runTest {
        val body = sse(
            """data: {"choices":[{"delta":{"reasoning_content":"weighing it"},"finish_reason":null}]}""",
            """data: {"choices":[{"delta":{"content":"Hel"}}]}""",
            """data: {"choices":[{"delta":{"content":"lo"}}]}""",
            "data: {\"choices\":[{\"delta\":{},\"finish_reason\":\"stop\"}]," +
                "\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":9}}",
            "data: [DONE]"
        )

        val (events, error) = read(body)

        assertNull(error)
        assertEquals(
            listOf(
                ChatStreamEvent.Reasoning("weighing it"),
                ChatStreamEvent.Content("Hel"),
                ChatStreamEvent.Content("lo"),
                ChatStreamEvent.Usage(promptTokens = 3, completionTokens = 9),
                ChatStreamEvent.Finished("stop")
            ),
            events
        )
    }

    @Test
    fun stopsAtTheDoneSentinelAndIgnoresLateData() = runTest {
        val body = sse(
            """data: {"choices":[{"delta":{"content":"hi"}}]}""",
            "data: [DONE]",
            """data: {"choices":[{"delta":{"content":"should not arrive"}}]}"""
        )

        val (events, error) = read(body)

        assertNull(error)
        assertEquals(listOf(ChatStreamEvent.Content("hi")), events)
    }

    @Test
    fun skipsBlankLinesCommentsAndUndecodablePayloads() = runTest {
        val body = sse(
            ": keep-alive",
            "",
            "data: not json at all",
            """data: {"choices":[{"delta":{"content":"survived"}}]}""",
            "data: [DONE]"
        )

        val (events, error) = read(body)

        assertNull(error)
        assertEquals(listOf(ChatStreamEvent.Content("survived")), events)
    }

    @Test
    fun reportsMalformedWhenTheBodyIsNotSseAtAll() = runTest {
        val (events, error) = read("<html><body>captive portal</body></html>")

        assertEquals(emptyList<ChatStreamEvent>(), events)
        assertEquals(ChatError.MalformedChunk, error)
    }

    @Test
    fun parsesSseLines() {
        assertEquals(SseLine.Done, parseSseLine("data: [DONE]"))
        assertEquals(SseLine.Done, parseSseLine("data:[DONE]"))
        assertEquals(SseLine.Ignored, parseSseLine(""))
        assertEquals(SseLine.Ignored, parseSseLine("   "))
        assertEquals(SseLine.Ignored, parseSseLine(": comment"))
        assertEquals(SseLine.Ignored, parseSseLine("event: message"))
        assertEquals(SseLine.Data("""{"a":1}"""), parseSseLine("""data: {"a":1}"""))
    }
}
