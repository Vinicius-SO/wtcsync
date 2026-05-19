package br.com.fiap.wtcsync.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.remote.ClientApi
import br.com.fiap.wtcsync.data.remote.MessageApi
import br.com.fiap.wtcsync.data.remote.dto.ClientDto
import br.com.fiap.wtcsync.util.NotificationHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val clients: List<ClientDto> = emptyList(),
    val searchQuery: String = "",
    val unreadCounts: Map<String, Int> = emptyMap(),
    val lastMessages: Map<String, String> = emptyMap()
) {
    val filteredClients: List<ClientDto>
        get() = if (searchQuery.isEmpty()) clients
                else clients.filter { it.name.contains(searchQuery, ignoreCase = true) }
}

class ChatListViewModel(
    private val clientApi: ClientApi,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val knownMessageIds = mutableSetOf<String>()
    private var initialLoadDone = false

    init {
        loadClients()
        startPolling()
    }

    fun loadClients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val clients = clientApi.getClients()
                _uiState.update { it.copy(clients = clients, isLoading = false) }
                refreshUnreadCounts(clients)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                val clients = _uiState.value.clients
                if (clients.isNotEmpty()) refreshUnreadCounts(clients)
            }
        }
    }

    private suspend fun refreshUnreadCounts(clients: List<ClientDto>) {
        val currentEmail = sessionManager.currentEmail ?: return
        val newCounts = mutableMapOf<String, Int>()
        val newLastMessages = mutableMapOf<String, String>()

        coroutineScope {
            clients.map { client ->
                async {
                    try {
                        val messages = messageApi.getInbox(client.id)
                        val received = messages.filter { it.senderId != currentEmail }

                        newCounts[client.id] = received.count { it.status != "ENTREGUE" }
                        newLastMessages[client.id] = messages
                            .maxByOrNull { it.createdAt ?: "" }?.text ?: ""

                        if (initialLoadDone) {
                            received
                                .filter { it.id !in knownMessageIds }
                                .forEach { msg ->
                                    NotificationHelper.showMessageNotification(
                                        context, client.name, msg.text, client.id
                                    )
                                }
                        }
                        received.forEach { knownMessageIds.add(it.id) }
                    } catch (_: Exception) {}
                }
            }.forEach { it.await() }
        }

        initialLoadDone = true
        _uiState.update { it.copy(unreadCounts = newCounts, lastMessages = newLastMessages) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

class ChatListViewModelFactory(
    private val clientApi: ClientApi,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatListViewModel(clientApi, messageApi, sessionManager, context) as T
}
