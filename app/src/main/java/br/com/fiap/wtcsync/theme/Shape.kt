package br.com.fiap.wtcsync.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Grid de raios: small=8, medium=12, large=20.
// CircleShape (50%) para avatares e FAB.
// Nenhum outro raio deve ser usado fora do theme.
val WtcShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
