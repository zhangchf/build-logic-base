package com.zcf.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zcf.chat.data.ChatRepository
import com.zcf.chat.domain.ChatEvent
import com.zcf.chat.domain.ChatMessage
import com.zcf.chat.domain.ChatRole
import com.zcf.chat.domain.ChatUiState
import com.zcf.network.deepseek.ChatStreamEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: ChatRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatUiState(apiKeyConfigured = repository.isConfigured)
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null
    private var nextId = 0L

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InputChanged -> _uiState.update { it.copy(input = event.text) }
            is ChatEvent.ModelSelected -> _uiState.update { it.copy(model = event.model) }
            is ChatEvent.ThinkingToggled -> _uiState.update { it.copy(thinkingEnabled = event.enabled) }
            is ChatEvent.ReasoningToggled -> updateMessage(event.messageId) {
                it.copy(reasoningExpanded = !it.reasoningExpanded)
            }

            ChatEvent.Send -> send()
            ChatEvent.Stop -> stop()
        }
    }

    private fun send() {
        val state = _uiState.value
        if (!state.canSend) return

        val userMessage = ChatMessage(
            id = nextId++,
            role = ChatRole.USER,
            content = state.input.trim()
        )
        val replyId = nextId++
        val placeholder = ChatMessage(id = replyId, role = ChatRole.ASSISTANT, isStreaming = true)
        // The placeholder being streamed into is deliberately left out of the history sent back.
        val history = state.messages + userMessage

        _uiState.update {
            it.copy(
                input = "",
                isStreaming = true,
                messages = it.messages + userMessage + placeholder
            )
        }

        streamJob = viewModelScope.launch {
            try {
                repository.stream(history, state.model, state.thinkingEnabled)
                    .collect { event -> apply(replyId, event) }
            } finally {
                // Runs on cancellation too, so Stop always leaves a consistent transcript rather
                // than a bubble stuck mid-stream.
                updateMessage(replyId) { it.copy(isStreaming = false) }
                _uiState.update { it.copy(isStreaming = false) }
            }
        }
    }

    private fun stop() {
        streamJob?.cancel()
        streamJob = null
    }

    private fun apply(messageId: Long, event: ChatStreamEvent) {
        when (event) {
            is ChatStreamEvent.Reasoning -> updateMessage(messageId) {
                it.copy(reasoning = it.reasoning + event.text)
            }

            is ChatStreamEvent.Content -> updateMessage(messageId) {
                it.copy(content = it.content + event.text)
            }

            is ChatStreamEvent.Usage -> updateMessage(messageId) {
                it.copy(
                    promptTokens = event.promptTokens,
                    completionTokens = event.completionTokens
                )
            }

            is ChatStreamEvent.Failed -> updateMessage(messageId) { it.copy(error = event.error) }
            is ChatStreamEvent.Finished -> Unit
        }
    }

    private fun updateMessage(id: Long, transform: (ChatMessage) -> ChatMessage) {
        _uiState.update { state ->
            state.copy(messages = state.messages.map { if (it.id == id) transform(it) else it })
        }
    }
}
