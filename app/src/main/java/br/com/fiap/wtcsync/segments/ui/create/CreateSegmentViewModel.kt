package br.com.fiap.wtcsync.segments.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.segments.data.dto.CreateSegmentDto
import br.com.fiap.wtcsync.segments.data.dto.SegmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateSegmentUiState(
    val name: String = "",
    val nameError: String? = null,
    val description: String = "",
    val tags: List<String> = listOf("Finance", "VIP"),
    val status: String = "Todos",
    val minScore: Int = 50,
    val clientCount: Int = 120,
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class CreateSegmentViewModel(
    private val repository: SegmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSegmentUiState())
    val uiState: StateFlow<CreateSegmentUiState> = _uiState

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onStatusChanged(value: String) {
        _uiState.value = _uiState.value.copy(status = value)
    }

    fun onScoreChanged(value: Int) {
        _uiState.value = _uiState.value.copy(minScore = value)
    }

    fun onRemoveTag(tag: String) {
        _uiState.value = _uiState.value.copy(
            tags = _uiState.value.tags.filter { it != tag }
        )
    }

    fun onAddTag(tag: String) {
        if (tag.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                tags = _uiState.value.tags + tag
            )
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "Nome obrigatório")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, error = null)
            try {
                repository.createSegment(
                    CreateSegmentDto(
                        name = state.name,
                        description = state.description,
                        tags = state.tags,
                        status = state.status,
                        minScore = state.minScore
                    )
                )
                _uiState.value = _uiState.value.copy(isSubmitting = false, success = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Erro ao criar segmento."
                )
            }
        }
    }
}