package br.com.fiap.wtcsync.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun WTCSYNCTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val scheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = if (darkTheme) Color(0xFFFFDC72) else Color(0xFF745B00),
        onPrimary = if (darkTheme) Color(0xFF302600) else Color.White,
        primaryContainer = palette.YellowBadge, onPrimaryContainer = palette.YellowText,
        secondary = palette.SaveText, onSecondary = palette.BackgroundCream,
        secondaryContainer = palette.ActionCardBg, onSecondaryContainer = palette.TextPrimary,
        tertiary = palette.InfoText, onTertiary = palette.InfoBg,
        tertiaryContainer = palette.InfoBg, onTertiaryContainer = palette.InfoText,
        background = palette.BackgroundCream, onBackground = palette.TextPrimary,
        surface = palette.CardSurface, onSurface = palette.TextPrimary,
        surfaceVariant = palette.CardGrayBg, onSurfaceVariant = palette.TextSecondary,
        surfaceDim = palette.BackgroundCream, surfaceBright = palette.CardSurface,
        surfaceContainerLowest = palette.BackgroundCream, surfaceContainerLow = palette.HeaderBg,
        surfaceContainer = palette.CardSurface, surfaceContainerHigh = palette.ActionCardBg,
        surfaceContainerHighest = palette.CardGrayBg,
        outline = palette.BorderColor, outlineVariant = palette.ListBorderLight,
        error = palette.ErrorText, onError = palette.ErrorBg,
        errorContainer = palette.ErrorBg, onErrorContainer = palette.ErrorText,
        inverseSurface = if (darkTheme) Color(0xFFF4F0E7) else Color(0xFF252521),
        inverseOnSurface = if (darkTheme) Color(0xFF252521) else Color(0xFFF4F0E7),
        inversePrimary = if (darkTheme) Color(0xFF745B00) else Color(0xFFFFDC72),
        surfaceTint = Color.Transparent
    )
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            @Suppress("DEPRECATION")
            window.statusBarColor = palette.BackgroundCream.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = palette.NavBarBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(LocalWtcPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = Typography) {
            Surface(color = palette.BackgroundCream, contentColor = palette.TextPrimary, content = content)
        }
    }
}
