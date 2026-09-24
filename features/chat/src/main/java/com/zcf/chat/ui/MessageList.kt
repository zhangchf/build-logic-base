package com.zcf.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zcf.chat.domain.ChatEvent
import com.zcf.chat.domain.ChatMessage

@Composable
internal fun MessageList(messages: List<ChatMessage>, onEvent: (ChatEvent) -> Unit, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val last = messages.lastOrNull()

    // A new turn always scrolls into view: the reader just sent it and expects to see it. This is
    // keyed on the count rather than the content, because the content-length effect below is
    // deliberately conditional and would otherwise swallow the first frame of a new message.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
    }

    // Token-by-token growth only follows if the reader is already at the bottom, so scrolling
    // back through the history is not yanked away mid-stream.
    LaunchedEffect(last?.content?.length, last?.reasoning?.length) {
        if (last != null && listState.firstVisibleItemIndex >= messages.lastIndex - 1) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = messages, key = { it.id }) { message ->
            MessageBubble(message = message, onEvent = onEvent)
        }
    }
}
