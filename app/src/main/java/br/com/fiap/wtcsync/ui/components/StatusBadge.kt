package br.com.fiap.wtcsync.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.theme.ListDraftBg
import br.com.fiap.wtcsync.theme.ListDraftText
import br.com.fiap.wtcsync.theme.ListScheduledBg
import br.com.fiap.wtcsync.theme.ListScheduledText
import br.com.fiap.wtcsync.theme.ListSentBg
import br.com.fiap.wtcsync.theme.ListSentText

@Composable
fun StatusBadge(status: String?) {
    val (bg, fg, label) = when (status?.lowercase()) {
        "active"   -> Triple(ListSentBg, ListSentText, "● ACTIVE")
        "prospect" -> Triple(ListScheduledBg, ListScheduledText, "● PROSPECT")
        else       -> Triple(ListDraftBg, ListDraftText, "● INACTIVE")
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            letterSpacing = 0.2.sp
        )
    }
}
