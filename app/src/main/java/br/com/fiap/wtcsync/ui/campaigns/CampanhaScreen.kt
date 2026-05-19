package br.com.fiap.wtcsync.ui.campaigns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.data.model.Campanha
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.FooterBg
import br.com.fiap.wtcsync.theme.FooterBorder
import br.com.fiap.wtcsync.theme.FooterText
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.InfoIconColor
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText

@Composable
fun CampanhaScreen(
    campanha: Campanha? = null,
    onBackClick: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        Header(
            onBackClick = onBackClick,
            onNotificationClick = { showDialog = true }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            EventBanner()
            ContentSection(
                title = campanha?.title ?: "Financial Shift 2025",
                date = campanha?.eventDate ?: "15 de Junho de 2025",
                location = campanha?.eventLocation ?: "WTC São Paulo, SP"
            )
            DescriptionCard(
                description = campanha?.eventDescription ?: "Prepare-se para a maior imersão em tendências do mercado financeiro da América Latina. O Financial Shift 2025 reunirá os principais arquitetos da economia digital para discutir estratégias de escala, conformidade e novas tecnologias de crédito. Uma oportunidade única de networking e insights estratégicos para o seu negócio."
            )
            ActionButtons()
            FooterTimestamp(
                text = campanha?.receivedDate ?: "Recebido em 12/05/2024 às 14:30"
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Fechar", color = YellowBadge, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.sino),
                        contentDescription = null,
                        tint = YellowBadge,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Notificações", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Nenhuma nova campanha no momento.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Fique atento! Novas promoções aparecerão aqui assim que forem lançadas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            containerColor = Color.White,
            tonalElevation = 6.dp
        )
    }
}

@Composable
private fun Header(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .height(56.dp)
            .drawBehind {
                drawLine(
                    color = BorderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderButton(
            onClick = onBackClick,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Voltar"
        )

        Text(
            text = "Campanha",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.18).sp
        )

        Surface(
            onClick = onNotificationClick,
            shape = RoundedCornerShape(8.dp),
            color = HeaderBg,
            border = BorderStroke(2.dp, BorderColor),
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sino),
                    contentDescription = "Notificações",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HeaderButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.shadow(2.dp, RoundedCornerShape(8.dp), ambientColor = BorderColor.copy(alpha = 0.4f), spotColor = BorderColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp),
        color = HeaderBg,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EventBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor, spotColor = BorderColor),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(193.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.publi_image),
                contentDescription = "Banner do Evento",
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun ContentSection(
    title: String,
    date: String,
    location: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BadgeExclusivo()

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-0.48).sp,
            lineHeight = 30.sp
        )

        Column(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow(icon = Icons.Default.CalendarMonth, text = date)
            InfoRow(icon = Icons.Default.LocationOn, text = location)
        }
    }
}

@Composable
private fun BadgeExclusivo() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = YellowBadge,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Text(
            text = "EXCLUSIVO",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 0.44.sp
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InfoIconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun DescriptionCard(
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SOBRE O EVENTO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.44.sp
            )

            Text(
                text = description,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
private fun ActionButtons() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActionButton(
            text = "GARANTIR VAGA",
            bgColor = YellowBadge,
            textColor = YellowText,
            icon = Icons.Default.Star,
            onClick = { },
            shadow = true
        )

        ActionButton(
            text = "VER PROGRAMAÇÃO",
            bgColor = Color.White,
            textColor = TextPrimary,
            icon = Icons.Default.ViewAgenda,
            onClick = { }
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    shadow: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (shadow) Modifier.shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor.copy(alpha = 0.4f), spotColor = BorderColor.copy(alpha = 0.4f))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (bgColor == YellowBadge) textColor else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.75.sp
            )
        }
    }
}

@Composable
private fun FooterTimestamp(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = FooterBg,
        border = BorderStroke(1.dp, FooterBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = FooterText,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FooterText,
                letterSpacing = 0.44.sp
            )
        }
    }
}
