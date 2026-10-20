package br.com.fiap.wtcsync.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteContrastTest {
    private fun readable(label: String, foreground: Color, background: Color) {
        val first = foreground.luminance().toDouble()
        val second = background.luminance().toDouble()
        val ratio = (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
        assertTrue("$label: contrast $ratio must be at least 4.5:1", ratio >= 4.5)
    }
    @Test fun bodyTextAndInputsStayReadableInBothModes() {
        for ((mode, p) in listOf("light" to LightPalette, "dark" to DarkPalette)) {
            for ((surface, background) in listOf("page" to p.BackgroundCream, "header" to p.HeaderBg,
                "card or input" to p.CardSurface, "action card" to p.ActionCardBg, "gray card" to p.CardGrayBg)) {
                readable("$mode $surface primary", p.TextPrimary, background)
                readable("$mode $surface secondary", p.TextSecondary, background)
                readable("$mode $surface placeholder", p.PlaceholderText, background)
            }
        }
    }
    @Test fun feedbackAndSelectionColorsHaveMatchingForegrounds() {
        for ((mode, p) in listOf("light" to LightPalette, "dark" to DarkPalette)) {
            for ((name, pair) in listOf("sent" to (p.ListSentText to p.ListSentBg),
                "scheduled" to (p.ListScheduledText to p.ListScheduledBg), "draft" to (p.ListDraftText to p.ListDraftBg),
                "error" to (p.ErrorText to p.ErrorBg), "success" to (p.SuccessText to p.SuccessBg),
                "info" to (p.InfoText to p.InfoBg), "amber label" to (p.YellowText to p.YellowBadge),
                "amber body" to (p.TextPrimary to p.YellowBadge), "schedule" to (p.AgendarText to p.AgendarBg),
                "footer" to (p.FooterText to p.FooterBg), "navigation" to (p.NavBarInactive to p.NavBarBackground),
                "selected navigation" to (p.NavBarActive to p.NavBarActiveFill))) {
                readable("$mode $name", pair.first, pair.second)
            }
        }
    }
}
