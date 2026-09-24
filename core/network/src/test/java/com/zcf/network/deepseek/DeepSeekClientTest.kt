package com.zcf.network.deepseek

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeepSeekClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun client(apiKey: String = TEST_KEY) = DeepSeekClient.create(
        apiKey = apiKey,
        ioDispatcher = Dispatchers.IO,
        baseUrl = server.url("/").toString()
    )

    private fun request() = ChatCompletionRequest(
        model = "deepseek-flash",
        messages = listOf(ChatMessageDto(role = "user", content = "hi")),
        stream = true
    )

    private fun streamed(body: String) = MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody(body)

    @Test
    fun postsToChatCompletionsWithABearerToken() = runBlocking {
        server.enqueue(streamed(SIMPLE_BODY))

        val events = client().streamChat(request()).toList()

        assertEquals(
            listOf(ChatStreamEvent.Content("Hello"), ChatStreamEvent.Finished("stop")),
            events
        )
        val recorded = server.takeRequest()
        assertEquals("/chat/completions", recorded.path)
        assertEquals("Bearer $TEST_KEY", recorded.getHeader("Authorization"))
    }

    @Test
    fun omitsAuthorizationWhenNoKeyIsConfigured() = runBlocking {
        server.enqueue(streamed(SIMPLE_BODY))

        client(apiKey = "").streamChat(request()).toList()

        assertEquals(null, server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun surfacesHttpFailuresWithTheApiMessage() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":{"message":"Authentication Fails","type":"authentication_error"}}""")
        )

        val events = client().streamChat(request()).toList()

        assertEquals(
            listOf(ChatStreamEvent.Failed(ChatError.Http(401, "Authentication Fails"))),
            events
        )
    }

    @Test
    fun streamingIncludesReasoningAndContentDeltas() = runBlocking {
        server.enqueue(streamed(REASONING_BODY))

        val events = client().streamChat(request()).toList()

        assertEquals(
            listOf(
                ChatStreamEvent.Reasoning("let me think"),
                ChatStreamEvent.Content("Hi"),
                ChatStreamEvent.Content(" there"),
                ChatStreamEvent.Finished("stop")
            ),
            events
        )
    }

    /**
     * The regression test for `call.cancel()`. The body is dripped at ~10KB/s, so it would take
     * roughly half a minute to drain. If cancelling the flow failed to close the socket, the
     * reader would stay parked in `readUtf8Line` and [cancelAndJoin] would block for that whole
     * time instead of returning immediately.
     */
    @Test
    fun cancellingTheCollectorUnblocksAStalledStream() = runBlocking {
        server.enqueue(streamed(STALLED_BODY).throttleBody(2_000, 1, TimeUnit.SECONDS))

        val received = mutableListOf<ChatStreamEvent>()
        val job = launch(Dispatchers.IO) {
            client().streamChat(request()).collect { received += it }
        }
        withTimeout(FIRST_EVENT_TIMEOUT_MS) {
            while (received.isEmpty()) delay(POLL_INTERVAL_MS)
        }

        val startedAt = System.nanoTime()
        job.cancelAndJoin()
        val elapsedMs = (System.nanoTime() - startedAt) / NANOS_PER_MILLI

        assertTrue("cancel took ${elapsedMs}ms, so the socket was not closed", elapsedMs < CANCEL_BUDGET_MS)
        assertEquals(ChatStreamEvent.Content("first"), received.first())
    }

    private companion object {
        const val TEST_KEY = "test-key"
        const val NANOS_PER_MILLI = 1_000_000L
        const val POLL_INTERVAL_MS = 10L
        const val FIRST_EVENT_TIMEOUT_MS = 15_000L
        const val CANCEL_BUDGET_MS = 5_000L

        val SIMPLE_BODY = """
            data: {"choices":[{"delta":{"content":"Hello"}}]}

            data: {"choices":[{"delta":{},"finish_reason":"stop"}]}

            data: [DONE]

        """.trimIndent()

        val REASONING_BODY = """
            data: {"choices":[{"delta":{"reasoning_content":"let me think"}}]}

            data: {"choices":[{"delta":{"content":"Hi"}}]}

            data: {"choices":[{"delta":{"content":" there"}}]}

            data: {"choices":[{"delta":{},"finish_reason":"stop"}]}

            data: [DONE]

        """.trimIndent()

        val STALLED_BODY: String = buildString {
            append("""data: {"choices":[{"delta":{"content":"first"}}]}""").append("\n\n")
            repeat(3_000) {
                append("""data: {"choices":[{"delta":{"content":"padding padding padding padding"}}]}""")
                append("\n\n")
            }
            append("data: [DONE]\n\n")
        }
    }
}
