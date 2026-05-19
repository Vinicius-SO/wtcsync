package br.com.fiap.wtcsync.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.theme.ListBorderLight
import br.com.fiap.wtcsync.theme.TextPrimary

@Composable
fun ClientAvatar(
    name: String,
    size: Dp = 44.dp,
    shape: Shape = CircleShape,
    showOnlineIndicator: Boolean = false
) {
    Box {
        Surface(
            modifier = Modifier.size(size),
            shape = shape,
            color = ListBorderLight,
            border = BorderStroke(1.5.dp, ListBorderLight)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = (size.value * 0.35f).sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}
