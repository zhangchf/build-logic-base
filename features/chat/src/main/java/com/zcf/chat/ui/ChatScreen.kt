package com.zcf.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zcf.chat.R
import com.zcf.chat.domain.ChatEvent
import com.zcf.chat.domain.ChatUiState

@Composable
fun ChatScreen(state: ChatUiState, onEvent: (ChatEvent) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ChatHeader(
                model = state.model,
                thinkingEnabled = state.thinkingEnabled,
                onEvent = onEvent
            )
        },
        bottomBar = {
            ChatInputBar(
                input = state.input,
                isStreaming = state.isStreaming,
                enabled = state.apiKeyConfigured,
                onEvent = onEvent
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (!state.apiKeyConfigured) {
                ApiKeyBanner()
            }
            MessageList(
                messages = state.messages,
                onEvent = onEvent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ApiKeyBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = stringResource(R.string.chat_missing_api_key),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}
