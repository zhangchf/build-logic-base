package com.zcf.chat.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zcf.chat.R
import com.zcf.chat.domain.ChatEvent
import com.zcf.chat.domain.ChatModel

@Composable
internal fun ChatHeader(
    model: ChatModel,
    thinkingEnabled: Boolean,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        // The padding goes on the inner Column rather than the Surface so the header's background
        // still paints behind the status bar, which is the point of drawing edge to edge.
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_title),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChatModel.entries.forEach { candidate ->
                    FilterChip(
                        selected = candidate == model,
                        onClick = { onEvent(ChatEvent.ModelSelected(candidate)) },
                        label = { Text(text = stringResource(candidate.labelRes())) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.chat_thinking_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Switch(
                    checked = thinkingEnabled,
                    onCheckedChange = { onEvent(ChatEvent.ThinkingToggled(it)) }
                )
            }
        }
    }
}

@StringRes
private fun ChatModel.labelRes(): Int = when (this) {
    ChatModel.FLASH -> R.string.chat_model_flash
    ChatModel.PRO -> R.string.chat_model_pro
}
