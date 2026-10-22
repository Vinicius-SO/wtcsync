package br.com.fiap.wtcsync.ui.components

import br.com.fiap.wtcsync.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcsync.theme.CardSurface
import br.com.fiap.wtcsync.theme.ListBorderLight

/** Upstream card interaction with the fork's paired light/dark palette. */
@Composable
fun WtcCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        interactionSource = interaction,
        shape = MaterialTheme.shapes.medium,
        color = CardSurface,
        border = WtcBorders.subtle(),
        shadowElevation = 1.dp
    ) { Box { content() } }
}
