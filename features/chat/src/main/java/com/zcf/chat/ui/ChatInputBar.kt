package com.zcf.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zcf.chat.R
import com.zcf.chat.domain.ChatEvent

@Composable
internal fun ChatInputBar(
    input: String,
    isStreaming: Boolean,
    enabled: Boolean,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            // union, not both insets: with the keyboard up the IME inset already covers the
            // navigation bar, so applying them separately would leave a gap above the keyboard.
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { onEvent(ChatEvent.InputChanged(it)) },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                maxLines = 4,
                placeholder = { Text(text = stringResource(R.string.chat_input_hint)) }
            )

            // While a reply is streaming the send button becomes stop, which is the only way to
            // abort a request that can otherwise run for minutes.
            if (isStreaming) {
                FilledIconButton(onClick = { onEvent(ChatEvent.Stop) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.chat_stop)
                    )
                }
            } else {
                FilledIconButton(
                    onClick = { onEvent(ChatEvent.Send) },
                    enabled = enabled && input.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send)
                    )
                }
            }
        }
    }
}
