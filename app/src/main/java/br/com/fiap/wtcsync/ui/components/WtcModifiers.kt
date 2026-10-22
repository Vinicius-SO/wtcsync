package br.com.fiap.wtcsync.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import br.com.fiap.wtcsync.theme.LocalReduceMotion
import br.com.fiap.wtcsync.theme.WtcMotion

// Escala 1→0.98 enquanto pressionado, reverte ao soltar.
// Sem efeito se LocalReduceMotion == true.
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val reduceMotion = LocalReduceMotion.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (!reduceMotion && isPressed) 0.98f else 1f,
        animationSpec = tween(WtcMotion.SHORT),
        label = "pressScale",
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}
