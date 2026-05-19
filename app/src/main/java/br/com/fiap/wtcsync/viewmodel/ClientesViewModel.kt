package br.com.fiap.wtcsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.remote.ClientApi
import br.com.fiap.wtcsync.data.remote.dto.ClientDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientesUiState(
    val clients: List<ClientDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "Todos"
)

class ClientesViewModel(private val clientApi: ClientApi) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    private var allClients: List<ClientDto> = emptyList()

    init { loadClients() }

    fun loadClients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                allClients = clientApi.getClients()
                _uiState.update { it.copy(isLoading = false) }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterChanged(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val query = _uiState.value.searchQuery
        val filter = _uiState.value.selectedFilter
        val filtered = allClients.filter { client ->
            val matchesQuery = query.isEmpty() ||
                client.name.contains(query, ignoreCase = true) ||
                client.email.contains(query, ignoreCase = true)
            val matchesFilter = filter == "Todos" ||
                client.status?.equals(filter, ignoreCase = true) == true
            matchesQuery && matchesFilter
        }
        _uiState.update { it.copy(clients = filtered) }
    }
}

class ClientesViewModelFactory(private val clientApi: ClientApi) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ClientesViewModel(clientApi) as T
}
