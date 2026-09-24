package com.zcf.chat.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zcf.chat.R
import com.zcf.network.deepseek.ChatError

/** Renders a terminal stream failure in place, so a partial reply keeps its context. */
@Composable
internal fun ChatErrorText(error: ChatError, modifier: Modifier = Modifier) {
    val text = when (error) {
        is ChatError.Http ->
            stringResource(R.string.chat_error_http, error.code, error.detail.orEmpty())

        is ChatError.Transport -> stringResource(R.string.chat_error_transport, error.message.orEmpty())
        ChatError.EmptyResponse -> stringResource(R.string.chat_error_empty_response)
        ChatError.MalformedChunk -> stringResource(R.string.chat_error_malformed)
        ChatError.MissingApiKey -> stringResource(R.string.chat_error_missing_key)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
    )
}
