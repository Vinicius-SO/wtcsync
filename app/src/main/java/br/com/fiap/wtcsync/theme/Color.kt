package br.com.fiap.wtcsync.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.DrawScope

@Immutable
data class WtcPalette(
    val Purple80: Color = Color(0xFFD0BCFF),
    val PurpleGrey80: Color = Color(0xFFCCC2DC),
    val Pink80: Color = Color(0xFFEFB8C8),
    val Purple40: Color = Color(0xFF6650a4),
    val PurpleGrey40: Color = Color(0xFF625b71),
    val Pink40: Color = Color(0xFF7D5260),
    val NavBarBackground: Color = Color(0xFFFCF9F8),
    val NavBarBorder: Color = Color(0xFF807660),
    val NavBarInactive: Color = Color(0xFF4E4633),
    val NavBarActive: Color = Color(0xFF695200),
    val NavBarActiveFill: Color = Color(0xFFF5C518),
    val BackgroundCream: Color = Color(0xFFFAF7F0),
    val HeaderBg: Color = Color(0xFFFCF9F8),
    val BorderColor: Color = Color(0xFF807660),
    val TextPrimary: Color = Color(0xFF1C1B1B),
    val TextSecondary: Color = Color(0xFF4E4633),
    // Brand actions stay vivid; containers and chat bubbles have their own semantics.
    val BrandAccent: Color = Color(0xFFF5C518),
    val OnBrandAccent: Color = Color(0xFF1C1B1B),
    val InputSurface: Color = Color(0xFFFFFFFF),
    val MessageSentBg: Color = Color(0xFFF5C518),
    val MessageSentText: Color = Color(0xFF594500),
    val YellowBadge: Color = Color(0xFFF5C518),
    val YellowText: Color = Color(0xFF594500),
    val FooterBg: Color = Color(0xFFE2DFDE),
    val FooterBorder: Color = Color(0xFFD1C5AC),
    val FooterText: Color = Color(0xFF474746),
    val InfoIconColor: Color = Color(0xFF745B00),
    val ListBorderLight: Color = Color(0xFFD1C5AC),
    val ListTabActive: Color = Color(0xFF745B00),
    val ListTabInactive: Color = Color(0xFF4E4633),
    val ListDivider: Color = Color(0xFFE5E2E1),
    val ListSentBg: Color = Color(0xFFC8E6C9),
    val ListSentText: Color = Color(0xFF1B5E20),
    val ListScheduledBg: Color = Color(0xFFBBDEFB),
    val ListScheduledText: Color = Color(0xFF0D47A1),
    val ListDraftBg: Color = Color(0xFFFFF9C4),
    val ListDraftText: Color = Color(0xFF8A4300),
    val ListStatsIcon: Color = Color(0xFF745B00),
    val ListScheduleIcon: Color = Color(0xFF636262),
    val ListScheduleBg: Color = Color(0xFFE2DFDE),
    val ListPromoBg: Color = Color(0xFF1C1B1B),
    val PlaceholderText: Color = Color(0xFF606773),
    val ActionCardBg: Color = Color(0xFFF6F3F2),
    val AgendarBg: Color = Color(0xFF8AD6ED),
    val AgendarText: Color = Color(0xFF005E70),
    val SaveText: Color = Color(0xFF745B00),
    val CardGrayBg: Color = Color(0xFFF0EDED),
    val CardSurface: Color = Color(0xFFFFFFFF),
    val InverseText: Color = Color(0xFFFFFFFF),
    val ErrorBg: Color = Color(0xFFFFEBEE),
    val ErrorText: Color = Color(0xFFB71C1C),
    val SuccessBg: Color = Color(0xFFE0FFE0),
    val SuccessText: Color = Color(0xFF2E7D32),
    val InfoBg: Color = Color(0xFFE8F4FD),
    val InfoText: Color = Color(0xFF0D47A1),
    val SkeletonBase: Color = Color(0xFFE8E5E0),
    val SkeletonHighlight: Color = Color(0xFFF5F3F0),
    val InputDisabled: Color = Color(0xFFE5E5E5),
)

val LightPalette = WtcPalette()
val DarkPalette = WtcPalette(
    InputSurface = Color(0xFF2D2D28),
    MessageSentBg = Color(0xFF39321C),
    MessageSentText = Color(0xFFF4F0E7),
    BackgroundCream = Color(0xFF171715),
    HeaderBg = Color(0xFF232320),
    CardSurface = Color(0xFF232320),
    TextPrimary = Color(0xFFF4F0E7),
    TextSecondary = Color(0xFFB8B3A7),
    BorderColor = Color(0xFF858578),
    NavBarBackground = Color(0xFF232320),
    NavBarBorder = Color(0xFF44443C),
    NavBarInactive = Color(0xFFB8B3A7),
    NavBarActive = Color(0xFFF5C518),
    NavBarActiveFill = Color(0xFF39321C),
    YellowBadge = Color(0xFF39321C),
    YellowText = Color(0xFFF7D978),
    FooterBg = Color(0xFF232320),
    FooterBorder = Color(0xFF44443C),
    FooterText = Color(0xFFB8B3A7),
    InfoIconColor = Color(0xFFF5C518),
    ListBorderLight = Color(0xFF44443C),
    ListTabActive = Color(0xFFF5C518),
    ListTabInactive = Color(0xFFB8B3A7),
    ListDivider = Color(0xFF383832),
    ListSentBg = Color(0xFF173D28),
    ListSentText = Color(0xFFB1E6BF),
    ListScheduledBg = Color(0xFF183851),
    ListScheduledText = Color(0xFFB8DDFF),
    ListDraftBg = Color(0xFF493B18),
    ListDraftText = Color(0xFFFFDE8A),
    ListStatsIcon = Color(0xFFF5C518),
    ListScheduleIcon = Color(0xFFB8B3A7),
    ListScheduleBg = Color(0xFF2D2D28),
    ListPromoBg = Color(0xFF0D0D0C),
    PlaceholderText = Color(0xFFB8B3A7),
    ActionCardBg = Color(0xFF2D2D28),
    AgendarBg = Color(0xFF123C48),
    AgendarText = Color(0xFFAAE8FA),
    SaveText = Color(0xFFF5C518),
    CardGrayBg = Color(0xFF2D2D28),
    ErrorBg = Color(0xFF4D2026),
    ErrorText = Color(0xFFFFB4BD),
    SuccessBg = Color(0xFF173D28),
    SuccessText = Color(0xFFB1E6BF),
    InfoBg = Color(0xFF183851),
    InfoText = Color(0xFFB8DDFF),
    SkeletonBase = Color(0xFF2D2D28),
    SkeletonHighlight = Color(0xFF3B3B34),
    InputDisabled = Color(0xFF32322D),
)
val LocalWtcPalette = staticCompositionLocalOf { LightPalette }

val Purple80: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.Purple80
val PurpleGrey80: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.PurpleGrey80
val Pink80: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.Pink80
val Purple40: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.Purple40
val PurpleGrey40: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.PurpleGrey40
val Pink40: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.Pink40
val NavBarBackground: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.NavBarBackground
val NavBarBorder: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.NavBarBorder
val NavBarInactive: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.NavBarInactive
val NavBarActive: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.NavBarActive
val NavBarActiveFill: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.NavBarActiveFill
val BackgroundCream: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.BackgroundCream
val HeaderBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.HeaderBg
val BorderColor: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.BorderColor
val TextPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.TextPrimary
val TextSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.TextSecondary
val YellowBadge: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.YellowBadge
val YellowText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.YellowText
val FooterBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.FooterBg
val FooterBorder: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.FooterBorder
val FooterText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.FooterText
val InfoIconColor: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.InfoIconColor
val ListBorderLight: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListBorderLight
val ListTabActive: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListTabActive
val ListTabInactive: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListTabInactive
val ListDivider: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListDivider
val ListSentBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListSentBg
val ListSentText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListSentText
val ListScheduledBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListScheduledBg
val ListScheduledText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListScheduledText
val ListDraftBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListDraftBg
val ListDraftText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListDraftText
val ListStatsIcon: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListStatsIcon
val ListScheduleIcon: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListScheduleIcon
val ListScheduleBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListScheduleBg
val ListPromoBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ListPromoBg
val PlaceholderText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.PlaceholderText
val ActionCardBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ActionCardBg
val AgendarBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.AgendarBg
val AgendarText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.AgendarText
val SaveText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.SaveText
val CardGrayBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.CardGrayBg
val CardSurface: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.CardSurface
val InverseText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.InverseText
val ErrorBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ErrorBg
val ErrorText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.ErrorText
val SuccessBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.SuccessBg
val SuccessText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.SuccessText
val InfoBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.InfoBg
val InfoText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.InfoText
val SkeletonBase: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.SkeletonBase
val SkeletonHighlight: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.SkeletonHighlight
val InputDisabled: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.InputDisabled

/** Capture the active palette before entering the non-composable drawing callback. */
@Composable
fun Modifier.themedDrawBehind(block: DrawScope.(WtcPalette) -> Unit): Modifier {
    val palette = LocalWtcPalette.current
    return drawBehind { block(palette) }
}

val BrandAccent: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.BrandAccent

val OnBrandAccent: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.OnBrandAccent

val InputSurface: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.InputSurface

val MessageSentBg: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.MessageSentBg

val MessageSentText: Color
    @Composable @ReadOnlyComposable get() = LocalWtcPalette.current.MessageSentText
