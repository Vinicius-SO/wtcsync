package br.com.fiap.wtcsync.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcsync.theme.LocalReduceMotion
import br.com.fiap.wtcsync.theme.WtcMotion

/** Animates the new tab only, without retaining old screens, observers or chat visibility. */
@Composable
fun Modifier.tabMotion(tab: Any): Modifier {
    val reduce = LocalReduceMotion.current
    val progress = remember { Animatable(1f) }
    val distance = with(LocalDensity.current) { 8.dp.toPx() }
    LaunchedEffect(tab, reduce) {
        if (reduce) progress.snapTo(1f)
        else {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(WtcMotion.SHORT, easing = WtcMotion.Standard))
        }
    }
    return graphicsLayer {
        alpha = if (reduce) 1f else progress.value
        translationY = if (reduce) 0f else distance * (1f - progress.value)
    }
}

@Composable
fun Modifier.wtcAnimateSize(): Modifier = animateContentSize(
    animationSpec = if (LocalReduceMotion.current) snap() else tween(WtcMotion.MEDIUM)
)

/** Stable item keys prevent polling/receipt updates from replaying appearance animation. */
@Composable
fun LazyItemScope.wtcItemMotion(): Modifier = Modifier.animateItem(
    fadeInSpec = if (LocalReduceMotion.current) snap() else tween(WtcMotion.SHORT),
    placementSpec = if (LocalReduceMotion.current) snap() else tween(WtcMotion.MEDIUM),
    fadeOutSpec = null
)
