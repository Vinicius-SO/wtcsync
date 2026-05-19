package br.com.fiap.wtcsync.campaigns.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.campaigns.data.CampaignRepository
import br.com.fiap.wtcsync.campaigns.domain.Campaign
import br.com.fiap.wtcsync.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CampaignListUiState(
    val campaigns: List<Campaign> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "Todas"
)

class CampaignListViewModel(
    private val repository: CampaignRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampaignListUiState())
    val uiState: StateFlow<CampaignListUiState> = _uiState

    private var allCampaigns: List<Campaign> = emptyList()

    init {
        loadCampaigns()
    }

    fun loadCampaigns() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.listCampaigns()) {
                is Resource.Success -> {
                    allCampaigns = result.data ?: emptyList()
                    applyFilters()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Erro ao carregar campanhas"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onFilterSelected(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = allCampaigns.filter { campanha ->
            val matchesSearch = state.searchQuery.isBlank() ||
                campanha.title.contains(state.searchQuery, ignoreCase = true) ||
                campanha.status.contains(state.searchQuery, ignoreCase = true)

            val matchesFilter = when (state.selectedFilter) {
                "Rascunho" -> campanha.status.equals("draft", ignoreCase = true)
                "Agendadas" -> campanha.status.equals("scheduled", ignoreCase = true)
                "Enviadas" -> campanha.status.equals("sent", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesFilter
        }
        _uiState.value = _uiState.value.copy(
            campaigns = filtered,
            isEmpty = filtered.isEmpty() && !state.isLoading
        )
    }

    fun refresh() {
        loadCampaigns()
    }
}
