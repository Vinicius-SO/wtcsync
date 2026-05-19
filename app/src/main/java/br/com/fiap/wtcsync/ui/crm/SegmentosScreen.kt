package br.com.fiap.wtcsync.ui.crm

import androidx.compose.runtime.Composable
import br.com.fiap.wtcsync.segments.ui.list.SegmentListScreen

@Composable
fun SegmentosScreen(
    onCreateClick: () -> Unit = {}
) {
    SegmentListScreen(
        onCreateClick = onCreateClick
    )
}