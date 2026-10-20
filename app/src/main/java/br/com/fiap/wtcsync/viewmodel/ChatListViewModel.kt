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
    val tickets: List<br.com.fiap.wtcsync.data.remote.SupportTicket> = emptyList(),
    val connected: Boolean = false,
    val searchQuery: String = "",
    val unreadCounts: Map<String, Int> = emptyMap(),
    val lastMessages: Map<String, String> = emptyMap(),
    val lastMessageTimes: Map<String, Long> = emptyMap()
) {
    val filteredClients: List<ClientDto>
        get() {
            val requestedTimes = tickets.associate { it.customerId to activityTime(it.requestedAt) }
            return clients
                .filter { searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) }
                .sortedWith(compareByDescending<ClientDto> {
                    lastMessageTimes[it.id] ?: requestedTimes[it.id] ?: Long.MIN_VALUE
                }.thenBy { it.id })
        }
}

// API messages use UTC local timestamps; support queue timestamps include their offset.
private fun activityTime(value: String?): Long {
    if (value.isNullOrBlank()) return Long.MIN_VALUE
    return runCatching { java.time.Instant.parse(value).toEpochMilli() }.getOrElse {
        runCatching { java.time.LocalDateTime.parse(value).toInstant(java.time.ZoneOffset.UTC).toEpochMilli() }
            .getOrDefault(Long.MIN_VALUE)
    }
}

class ChatListViewModel(
    private val clientApi: ClientApi,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val context: Context,
    private val supportApi: br.com.fiap.wtcsync.data.remote.SupportApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val knownMessageIds = mutableSetOf<String>()
    private var initialLoadDone = false

    init {
        loadClients()
        startPolling()
        viewModelScope.launch {
            br.com.fiap.wtcsync.data.remote.SupportSocket(sessionManager).listen(
                { connected -> _uiState.update { it.copy(connected = connected) } },
                { loadClients(silent = true) }
            )
        }
    }

    fun loadClients(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tickets = supportApi.queue()
                val clients = tickets.map { ticket -> ClientDto(ticket.customerId, ticket.customerName ?: "Cliente", "", null, null, null, null, null) }
                _uiState.update { it.copy(clients = clients, tickets = tickets, isLoading = false, error = null) }
                refreshUnreadCounts(clients)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                loadClients(silent = true)
            }
        }
    }

    private suspend fun refreshUnreadCounts(clients: List<ClientDto>) {
        val currentEmail = sessionManager.currentEmail ?: return
        val newCounts = mutableMapOf<String, Int>()
        val newLastMessages = mutableMapOf<String, String>()
        val newLastMessageTimes = mutableMapOf<String, Long>()

        coroutineScope {
            clients.map { client ->
                async {
                    try {
                        // Busca apenas a conversa deste operador com este cliente
                        val messages = messageApi.getConversation(
                            senderId   = currentEmail,
                            customerId = client.id
                        )
                        // "received" são as que vieram do cliente (não do operador)
                        val customerEmail = _uiState.value.tickets.firstOrNull { it.customerId == client.id }?.customerEmail
                        val received = messages.filter { it.senderId == customerEmail }

                        newCounts[client.id] = received.count { it.status == "ENVIADO" }
                        val latest = messages.maxByOrNull { activityTime(it.createdAt) }
                        newLastMessages[client.id] = latest?.text ?: ""
                        latest?.let { activityTime(it.createdAt) }?.takeIf { it != Long.MIN_VALUE }
                            ?.let { newLastMessageTimes[client.id] = it }

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
        _uiState.update { it.copy(
            unreadCounts = newCounts,
            lastMessages = it.lastMessages + newLastMessages,
            lastMessageTimes = it.lastMessageTimes + newLastMessageTimes
        ) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

class ChatListViewModelFactory(
    private val clientApi: ClientApi,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val context: Context,
    private val supportApi: br.com.fiap.wtcsync.data.remote.SupportApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatListViewModel(clientApi, messageApi, sessionManager, context, supportApi) as T
}
