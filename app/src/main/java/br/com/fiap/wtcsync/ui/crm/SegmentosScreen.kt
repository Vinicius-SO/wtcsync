package br.com.fiap.wtcsync.ui.crm

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.segments.ui.list.SegmentListScreen
import br.com.fiap.wtcsync.segments.ui.list.SegmentListViewModel
import br.com.fiap.wtcsync.segments.ui.list.SegmentListViewModelFactory

@Composable
fun SegmentosScreen(
    application: Application,
    userRole: UserRole?,
    onCreateClick: () -> Unit = {}
) {
    val viewModel: SegmentListViewModel = viewModel(
        factory = SegmentListViewModelFactory(application)
    )
    val state by viewModel.uiState.collectAsState()

    SegmentListScreen(
        segments = state.segments,
        isLoading = state.isLoading,
        error = state.error,
        showCreateButton = userRole != UserRole.CLIENTE,
        onCreateClick = onCreateClick,
        onRetry = { viewModel.loadSegments() }
    )
}
