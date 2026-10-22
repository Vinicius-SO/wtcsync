package br.com.fiap.wtcsync.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Decorative outlines never replace the stronger boundary of an input or control. */
object WtcBorders {
    val Width = 1.dp
    val FocusWidth = 2.dp

    @Composable fun subtle() = BorderStroke(Width, ListBorderLight)
    @Composable fun control() = BorderStroke(Width, BorderColor)
    @Composable fun selected() = BorderStroke(Width, SaveText)
}
