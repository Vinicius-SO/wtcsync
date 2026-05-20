package br.com.fiap.wtcsync.segments.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.segments.data.dto.SegmentDto
import br.com.fiap.wtcsync.theme.*

@Composable
fun SegmentListScreen(
    segments: List<SegmentDto> = emptyList(),
    isLoading: Boolean = false,
    error: String? = null,
    showCreateButton: Boolean = true,
    onCreateClick: () -> Unit = {},
    onSegmentClick: (String) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = segments.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        // Header
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
                text = "Segmentos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (showCreateButton) {
                Surface(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(4.dp))
                        .clickable(onClick = onCreateClick),
                    shape = RoundedCornerShape(4.dp),
                    color = YellowBadge,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                            text = "+ Novo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowText
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        color = Color.White,
                        border = BorderStroke(2.dp, BorderColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(start = 40.dp, end = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar segmentos...",
                                    fontSize = 15.sp,
                                    color = TextSecondary
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                ),
                                cursorBrush = SolidColor(TextPrimary),
                                singleLine = true,
                                decorationBox = { innerTextField -> innerTextField() }
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .padding(start = 11.dp, top = 15.dp)
                            .size(18.dp)
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier
                                .padding(end = 11.dp, top = 15.dp)
                                .size(18.dp)
                                .align(Alignment.CenterEnd)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            if (isLoading) {
                items(3) { SegmentSkeletonCard() }
            } else if (error != null) {
                item {
                    ErrorState(message = error, onRetry = onRetry)
                }
            } else if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        showCreateButton = showCreateButton,
                        onCreateClick = onCreateClick
                    )
                }
            } else {
                items(filtered, key = { it.id }) { segment ->
                    SegmentCard(
                        segment = segment,
                        onClick = { onSegmentClick(segment.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentCard(
    segment: SegmentDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(0.dp), ambientColor = BorderColor, spotColor = BorderColor)
            .clickable(onClick = onClick),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = segment.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${segment.clientCount} clientes",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    segment.tags.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = YellowBadge,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = YellowText
                            )
                        }
                    }
                }
            }

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(0.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ver",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = YellowBadge,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Enviar campanha",
                            tint = YellowText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentSkeletonCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp, 18.dp)
                    .background(Color(0xFFE8E5E0), RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .size(100.dp, 14.dp)
                    .background(Color(0xFFE8E5E0), RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFFE8E5E0), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Erro ao carregar segmentos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = message,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(8.dp),
                color = YellowBadge,
                border = BorderStroke(2.dp, BorderColor)
            ) {
                Text(
                    text = "Tentar novamente",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = YellowText
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    showCreateButton: Boolean,
    onCreateClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Crie novos filtros",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Organize sua base de contatos por interesses ou localização.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (showCreateButton) {
                Surface(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(8.dp),
                    color = YellowBadge,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Text(
                        text = "+ Novo Segmento",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowText
                    )
                }
            }
        }
    }
}