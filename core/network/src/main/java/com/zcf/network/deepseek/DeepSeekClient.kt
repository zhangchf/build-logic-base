package com.zcf.network.deepseek

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val CONNECT_TIMEOUT_SECONDS = 30L

// Thinking can run for minutes with no bytes on the wire. OkHttp's 10 second default would abort
// the stream long before the first token arrives.
private const val READ_TIMEOUT_MINUTES = 5L
private const val JSON_MEDIA_TYPE = "application/json"
private const val AUTHORIZATION_HEADER = "Authorization"

class DeepSeekClient internal constructor(
    private val api: DeepSeekApi,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Streams one chat completion. Emits [ChatStreamEvent]s ending in either
     * [ChatStreamEvent.Finished] or [ChatStreamEvent.Failed]; the only exception a collector can
     * see is cancellation, which is what [DeepSeekClient] is cancelled with.
     */
    fun streamChat(request: ChatCompletionRequest): Flow<ChatStreamEvent> = callbackFlow {
        val call = api.streamChat(request)
        val producer = launch(ioDispatcher) {
            val error = try {
                emitStream(call) { event -> send(event) }
            } catch (e: IOException) {
                ChatError.Transport(e.message)
            }
            if (error != null) {
                // trySend, not send: on a cancel the collector is already gone and this is a no-op.
                trySend(ChatStreamEvent.Failed(error))
            }
            close()
        }
        awaitClose {
            // Cancelling the call closes the socket, which unblocks the reader sitting in
            // readUtf8Line. Without this the thread would stay parked until the server spoke again.
            call.cancel()
            producer.cancel()
        }
    }

    private suspend fun emitStream(call: Call<ResponseBody>, emit: suspend (ChatStreamEvent) -> Unit): ChatError? {
        val response = call.execute()
        val body = response.body()
        return when {
            !response.isSuccessful -> {
                val detail = response.errorBody()?.use { decodeErrorMessage(json, it.string()) }
                ChatError.Http(response.code(), detail)
            }

            body == null -> ChatError.EmptyResponse
            else -> body.use { readChatStream(it.source(), json, emit) }
        }
    }

    companion object {
        /**
         * Builds a client for [baseUrl]. The key is optional so the app can start, and report a
         * clear local error, before one has been configured; a blank key sends no Authorization
         * header rather than a malformed `Bearer `.
         */
        fun create(
            apiKey: String,
            ioDispatcher: CoroutineDispatcher,
            baseUrl: String = DeepSeekApi.BASE_URL
        ): DeepSeekClient {
            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
                isLenient = true
            }

            val httpClient = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .callTimeout(0, TimeUnit.MILLISECONDS)
                .addInterceptor { chain ->
                    val builder = chain.request().newBuilder()
                    if (apiKey.isNotBlank()) {
                        builder.header(AUTHORIZATION_HEADER, "Bearer $apiKey")
                    }
                    chain.proceed(builder.build())
                }
                .build()

            val api = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
                .build()
                .create(DeepSeekApi::class.java)

            return DeepSeekClient(api, json, ioDispatcher)
        }
    }
}
