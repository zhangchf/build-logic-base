package com.zcf.chat.domain

/**
 * Everything the chat screen can ask for. A single sink keeps the screen's signature inside
 * detekt's `LongParameterList` budget and leaves room for new interactions without widening it.
 */
sealed interface ChatEvent {
    data class InputChanged(val text: String) : ChatEvent
    data class ModelSelected(val model: ChatModel) : ChatEvent
    data class ThinkingToggled(val enabled: Boolean) : ChatEvent
    data class ReasoningToggled(val messageId: Long) : ChatEvent
    data object Send : ChatEvent
    data object Stop : ChatEvent
}
