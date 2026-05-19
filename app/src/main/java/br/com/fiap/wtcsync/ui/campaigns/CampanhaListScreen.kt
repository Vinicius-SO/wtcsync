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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.data.model.Campanha
import br.com.fiap.wtcsync.data.model.mockCampanhas
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.FooterBg
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.ListBorderLight
import br.com.fiap.wtcsync.theme.ListDivider
import br.com.fiap.wtcsync.theme.ListDraftBg
import br.com.fiap.wtcsync.theme.ListDraftText
import br.com.fiap.wtcsync.theme.ListPromoBg
import br.com.fiap.wtcsync.theme.ListScheduleBg
import br.com.fiap.wtcsync.theme.ListScheduleIcon
import br.com.fiap.wtcsync.theme.ListScheduledBg
import br.com.fiap.wtcsync.theme.ListScheduledText
import br.com.fiap.wtcsync.theme.ListSentBg
import br.com.fiap.wtcsync.theme.ListSentText
import br.com.fiap.wtcsync.theme.ListStatsIcon
import br.com.fiap.wtcsync.theme.ListTabActive
import br.com.fiap.wtcsync.theme.ListTabInactive
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText

@Composable
fun CampanhaListScreen(
    onCampanhaClick: (Campanha) -> Unit,
    onCreateClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1200)
        isLoading = false
    }

    val filteredCampanhas = remember(searchQuery, selectedFilter) {
        mockCampanhas.filter { campanha ->
            val matchesSearch = searchQuery.isBlank() ||
                campanha.title.contains(searchQuery, ignoreCase = true) ||
                campanha.status.contains(searchQuery, ignoreCase = true) ||
                campanha.eventLocation.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Rascunho" -> campanha.status == "RASCUNHO"
                "Agendadas" -> campanha.status == "AGENDADA"
                "Enviadas" -> campanha.status == "ENVIADA"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        ListHeader(onCreateClick = onCreateClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
            FilterTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            if (isLoading) {
                SkeletonList()
            } else {
                CampaignCards(
                    campanhas = filteredCampanhas,
                    onCampanhaClick = onCampanhaClick
                )
            }
            PromoCard()
        }
    }
}

@Composable
private fun ListHeader(onCreateClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .height(56.dp)
            .drawBehind {
                drawLine(
                    color = ListBorderLight,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(2.dp, ListBorderLight)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wtcsync_logo),
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            }

            Text(
                text = "Campanhas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.18).sp
            )
        }

        Surface(
            modifier = Modifier
                .shadow(2.dp, RoundedCornerShape(4.dp), ambientColor = BorderColor.copy(alpha = 0.4f), spotColor = BorderColor.copy(alpha = 0.4f))
                .clickable(onClick = onCreateClick),
            shape = RoundedCornerShape(4.dp),
            color = YellowBadge,
            border = BorderStroke(2.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            color = Color.White,
            border = BorderStroke(2.dp, ListBorderLight)
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
                        text = "Buscar campanhas...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
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
            contentDescription = "Buscar",
            tint = if (searchQuery.isNotEmpty()) TextPrimary else TextSecondary,
            modifier = Modifier
                .padding(start = 11.dp, top = 15.dp)
                .size(18.dp)
        )

        if (searchQuery.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Limpar",
                tint = TextSecondary,
                modifier = Modifier
                    .padding(end = 11.dp, top = 15.dp)
                    .size(18.dp)
                    .align(Alignment.CenterEnd)
                    .clickable { onSearchQueryChange("") }
            )
        }
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val labels = listOf("Todas", "Rascunho", "Agendadas", "Enviadas")
    val tabPositions = remember { mutableStateOf<Map<String, Pair<Float, Float>>>(emptyMap()) }

    val target = tabPositions.value[selectedFilter]
    val targetOffset = target?.first ?: 0f
    val targetWidth = target?.second ?: 0f

    val indicatorOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 250),
        label = "offset"
    )
    val indicatorWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = 250),
        label = "width"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = ListBorderLight,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
                drawLine(
                    color = ListTabActive,
                    start = Offset(indicatorOffset, size.height),
                    end = Offset(indicatorOffset + indicatorWidth, size.height),
                    strokeWidth = 4f
                )
            }
    ) {
        labels.forEach { label ->
            FilterTab(
                label = label,
                isActive = selectedFilter == label,
                onClick = { onFilterSelected(label) },
                onPositioned = { x, width ->
                            tabPositions.value = tabPositions.value + (label to (x to width))
                }
            )
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onPositioned: (Float, Float) -> Unit
) {
    val textColor = if (isActive) ListTabActive else ListTabInactive

    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .onGloballyPositioned { coords ->
                onPositioned(coords.positionInParent().x, coords.size.width.toFloat())
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.44.sp
        )
    }
}

@Composable
private fun CampaignCards(
    campanhas: List<Campanha>,
    onCampanhaClick: (Campanha) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        campanhas.forEach { campanha ->
            CampaignCard(
                campanha = campanha,
                onClick = { onCampanhaClick(campanha) }
            )
        }
    }
}

@Composable
private fun CampaignCard(
    campanha: Campanha,
    onClick: () -> Unit
) {
    val (statusBg, statusTextColor) = when (campanha.status) {
        "ENVIADA" -> ListSentBg to ListSentText
        "AGENDADA" -> ListScheduledBg to ListScheduledText
        else -> ListDraftBg to ListDraftText
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(0.dp), ambientColor = BorderColor, spotColor = BorderColor)
            .clickable(onClick = onClick),
        color = Color.White,
        border = BorderStroke(2.dp, ListBorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = campanha.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal,
                            color = statusTextColor,
                            letterSpacing = 0.4.sp
                        )
                    }

                    Text(
                        text = campanha.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.18).sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Mais",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            when (campanha.status) {
                "ENVIADA" -> SentFooter(campanha)
                "AGENDADA" -> ScheduledFooter(campanha)
                "RASCUNHO" -> DraftFooter(campanha)
            }
        }
    }
}

@Composable
private fun SentFooter(campanha: Campanha) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = ListDivider,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
            .padding(top = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (campanha.statsSent != null) {
                StatsRow(count = "${campanha.statsSent} entregues")
            }
            if (campanha.statsRead != null) {
                StatsRow(count = "${campanha.statsRead} lidas")
            }
        }
    }
}

@Composable
private fun StatsRow(count: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .background(ListStatsIcon, CircleShape)
        )
        Text(
            text = count,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ScheduledFooter(campanha: Campanha) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ListScheduleBg)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(ListScheduleIcon)
        )
        Text(
            text = campanha.scheduleInfo ?: "",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ListScheduleIcon,
            letterSpacing = 0.44.sp
        )
    }
}

@Composable
private fun DraftFooter(campanha: Campanha) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(0.dp),
            color = Color.White,
            border = BorderStroke(2.dp, ListBorderLight),
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Editar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(0.dp),
            color = YellowBadge,
            border = BorderStroke(2.dp, BorderColor),
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enviar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun SkeletonList() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) { SkeletonCard() }
    }
}

@Composable
private fun SkeletonCard() {
    val shimmerColors = listOf(
        Color(0xFFE8E5E0),
        Color(0xFFF5F3F0),
        Color(0xFFE8E5E0)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(0.dp), ambientColor = BorderColor, spotColor = BorderColor),
        color = Color.White,
        border = BorderStroke(2.dp, ListBorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DrawShimmerRect(brush = brush, width = 60, height = 14)
                    DrawShimmerRect(brush = brush, width = 180, height = 20)
                }
                DrawShimmerRect(brush = brush, width = 4, height = 16)
            }
            DrawShimmerLine(brush = brush)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DrawShimmerRect(brush = brush, width = 90, height = 14)
                DrawShimmerRect(brush = brush, width = 70, height = 14)
            }
        }
    }
}

@Composable
private fun DrawShimmerRect(brush: Brush, width: Int, height: Int) {
    Box(
        modifier = Modifier
            .size(width.dp, height.dp)
            .background(brush, RoundedCornerShape(2.dp))
    )
}

@Composable
private fun DrawShimmerLine(brush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(brush)
    )
}

@Composable
private fun PromoCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(0.dp), ambientColor = BorderColor, spotColor = BorderColor),
        color = ListPromoBg,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = YellowBadge.copy(alpha = 0.3f),
                modifier = Modifier.size(44.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Potencialize seus resultados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    letterSpacing = (-0.18).sp,
                    lineHeight = 24.sp
                )

                Text(
                    text = "Crie automações baseadas no comportamento do seu cliente.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp,
                    modifier = Modifier.width(200.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(0.dp),
                color = YellowBadge,
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Explorar Templates",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}
