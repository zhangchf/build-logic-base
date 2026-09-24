package com.zcf.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zcf.chat.R
import com.zcf.chat.domain.ChatEvent
import com.zcf.chat.domain.ChatMessage
import com.zcf.chat.domain.ChatRole

@Composable
internal fun MessageBubble(message: ChatMessage, onEvent: (ChatEvent) -> Unit, modifier: Modifier = Modifier) {
    val isUser = message.role == ChatRole.USER
    val promptTokens = message.promptTokens
    val completionTokens = message.completionTokens

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (message.reasoning.isNotEmpty()) {
                    ReasoningPanel(
                        reasoning = message.reasoning,
                        expanded = message.reasoningExpanded,
                        streaming = message.isStreaming,
                        onToggle = { onEvent(ChatEvent.ReasoningToggled(message.id)) }
                    )
                }

                if (message.content.isEmpty() && message.isStreaming) {
                    Text(
                        text = stringResource(R.string.chat_pending),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (message.content.isNotEmpty()) {
                    Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
                }

                message.error?.let { ChatErrorText(error = it) }

                if (promptTokens != null && completionTokens != null) {
                    Text(
                        text = stringResource(R.string.chat_tokens, promptTokens, completionTokens),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
