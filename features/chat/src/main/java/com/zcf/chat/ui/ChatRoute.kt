package com.zcf.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful entry point: wires the screen to its ViewModel. */
@Composable
fun ChatRoute(modifier: Modifier = Modifier, viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}
