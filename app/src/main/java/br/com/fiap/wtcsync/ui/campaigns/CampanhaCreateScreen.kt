package br.com.fiap.wtcsync.ui.campaigns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.theme.ActionCardBg
import br.com.fiap.wtcsync.theme.AgendarBg
import br.com.fiap.wtcsync.theme.AgendarText
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.CardGrayBg
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.SaveText
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText

@Composable
fun CampanhaCreateScreen(
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        CreateHeader(onCancelClick = onCancelClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            BasicInfoSection()
            MediaSection()
            LinkSection()
            ActionButtonsSection()
        }

        CreateFooter()
    }
}

@Composable
private fun CreateHeader(onCancelClick: () -> Unit) {
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
        Text(
            text = "CANCELAR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp,
            modifier = Modifier.clickable(onClick = onCancelClick)
        )

        Text(
            text = "Nova Campanha",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.18).sp
        )

        Text(
            text = "SALVAR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SaveText,
            letterSpacing = 0.44.sp
        )
    }
}

@Composable
private fun BasicInfoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FormField(
            label = "TÍTULO DA CAMPANHA",
            placeholder = "Ex: Promoção de Verão",
            height = 48
        )

        FormField(
            label = "MENSAGEM",
            placeholder = "Digite o conteúdo da sua notificação...",
            isMultiline = true,
            height = 120
        )

        SegmentField()
    }
}

@Composable
private fun FormField(
    label: String,
    placeholder: String,
    isMultiline: Boolean = false,
    height: Int = 48
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.dp, BorderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isMultiline) height.dp else height.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = if (isMultiline) Alignment.TopStart else Alignment.CenterStart
            ) {
                Text(
                    text = placeholder,
                    fontSize = if (isMultiline) 13.sp else 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    lineHeight = if (isMultiline) 22.sp else 20.sp
                )
            }
        }
    }
}

@Composable
private fun SegmentField() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "SELECIONAR SEGMENTO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(2.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Todos os Clientes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BANNER DA CAMPANHA (16:9)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.44.sp
            )

            Surface(
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor.copy(alpha = 0.4f), spotColor = BorderColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                color = YellowBadge,
                border = BorderStroke(2.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = YellowText,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Adicionar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowText,
                        letterSpacing = 0.44.sp
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = CardGrayBg,
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
                    contentDescription = "Banner da Campanha",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LinkSection() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "DEEPLINK / URL DE DESTINO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "https://app.wtcsync.com/promo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ActionCardBg,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "BOTÕES DE AÇÃO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.44.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButtonColumn(
                    label = "BOTÃO PRIMÁRIO",
                    buttonPlaceholder = "Comprar Agora",
                    linkPlaceholder = "Link de ação"
                )

                ActionButtonColumn(
                    label = "BOTÃO SECUNDÁRIO (OPCIONAL)",
                    buttonPlaceholder = "Texto do botão",
                    linkPlaceholder = "Link de ação"
                )
            }
        }
    }
}

@Composable
private fun ActionButtonColumn(
    label: String,
    buttonPlaceholder: String,
    linkPlaceholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp,
            lineHeight = 16.sp
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(2.dp, BorderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = buttonPlaceholder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(2.dp, BorderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = linkPlaceholder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun CreateFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HeaderBg.copy(alpha = 0.8f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = BorderColor.copy(alpha = 0.6f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2f
                    )
                }
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor.copy(alpha = 0.4f), spotColor = BorderColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Salvar Rascunho",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor.copy(alpha = 0.4f), spotColor = BorderColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    color = AgendarBg,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Agendar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgendarText
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor, spotColor = BorderColor),
                shape = RoundedCornerShape(12.dp),
                color = YellowBadge,
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
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = YellowText,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Enviar Agora",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = YellowText,
                        letterSpacing = (-0.18).sp
                    )
                }
            }
        }
    }
}
