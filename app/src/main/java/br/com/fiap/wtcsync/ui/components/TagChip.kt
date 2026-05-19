package br.com.fiap.wtcsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.theme.ListBorderLight
import br.com.fiap.wtcsync.theme.ListScheduledBg
import br.com.fiap.wtcsync.theme.ListScheduledText
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText

@Composable
fun TagChip(label: String, onRemove: (() -> Unit)? = null) {
    val (bg, fg) = when (label.uppercase()) {
        "VIP"     -> YellowBadge.copy(alpha = 0.25f) to YellowText
        "FINANCE" -> ListScheduledBg to ListScheduledText
        else      -> ListBorderLight to TextSecondary
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg)
            if (onRemove != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover",
                    tint = fg,
                    modifier = Modifier.size(12.dp).clickable { onRemove() }
                )
            }
        }
    }
}
