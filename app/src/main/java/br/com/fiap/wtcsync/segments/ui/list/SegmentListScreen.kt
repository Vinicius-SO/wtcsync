package br.com.fiap.wtcsync.segments.ui.list

import br.com.fiap.wtcsync.ui.components.*
import br.com.fiap.wtcsync.theme.*
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
import br.com.fiap.wtcsync.theme.themedDrawBehind
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
        WtcScreenHeader("Segmentos", actions = {
            if (showCreateButton) WtcCreateButton(onCreateClick)
        })

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
                        color = CardSurface,
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
        color = CardSurface,
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
                    color = CardSurface,
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
        color = CardSurface,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp, 18.dp)
                    .background(SkeletonBase, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .size(100.dp, 14.dp)
                    .background(SkeletonBase, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(SkeletonBase, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    WtcStatePanel("Não foi possível carregar os segmentos", message, onRetry = onRetry)
}

@Composable
private fun EmptyState(
    showCreateButton: Boolean,
    onCreateClick: () -> Unit
) {
    WtcStatePanel("Nenhum segmento encontrado", action = {
        if (showCreateButton) { Spacer(Modifier.height(16.dp)); WtcCreateButton(onCreateClick) }
    })
}