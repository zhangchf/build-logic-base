package com.zcf.network.deepseek

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

internal interface DeepSeekApi {
    /**
     * Returns a [Call] rather than being a suspend function on purpose. A suspend function leaves
     * no handle for the caller to abort with, and a blocked `readUtf8Line` is not a coroutine
     * suspension point, so cancelling the collector would not stop the request. Exposing the
     * [Call] lets [DeepSeekClient] cancel it, which closes the socket and unblocks the reader.
     *
     * [Streaming] keeps the body off-heap so chunks are readable as they arrive.
     */
    @Streaming
    @POST("chat/completions")
    fun streamChat(@Body request: ChatCompletionRequest): Call<ResponseBody>

    companion object {
        /** Retrofit requires the base URL to end in a slash. */
        const val BASE_URL: String = "https://api.deepseek.com/"
    }
}
