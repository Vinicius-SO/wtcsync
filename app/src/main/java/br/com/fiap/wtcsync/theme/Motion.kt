package br.com.fiap.wtcsync.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

// Tokens de duração e easing — curtos, sem bounce, nada acima de 350ms.
object WtcMotion {
    const val SHORT  = 150  // hover/press, cor, ícones
    const val MEDIUM = 250  // entrada de itens, fade de conteúdo
    const val LONG   = 350  // transição entre telas

    val Emphasized           = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val Standard             = FastOutSlowInEasing
}

// Respeita a preferência do sistema "remover animações".
// Se ANIMATOR_DURATION_SCALE == 0, desative stagger e scale; mantenha apenas fades curtos.
val LocalReduceMotion = compositionLocalOf { false }

@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    val scale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )
    return scale == 0f
}
