package br.com.fiap.wtcsync.segments.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.segments.data.dto.SegmentDto
import br.com.fiap.wtcsync.segments.data.dto.SegmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SegmentListUiState(
    val segments: List<SegmentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SegmentListViewModel(
    private val repository: SegmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SegmentListUiState())
    val uiState: StateFlow<SegmentListUiState> = _uiState

    init {
        loadSegments()
    }

    fun loadSegments() {
        viewModelScope.launch {
            _uiState.value = SegmentListUiState(isLoading = true)
            try {
                val segments = repository.getSegments()
                val activeSegments = segments.filter {
                    it.status.equals("active", ignoreCase = true)
                }
                _uiState.value = SegmentListUiState(segments = activeSegments)
            } catch (e: Exception) {
                _uiState.value = SegmentListUiState(
                    error = e.message ?: "Erro ao carregar segmentos"
                )
            }
        }
    }
}
