package br.com.fiap.wtcsync.ui.components

import br.com.fiap.wtcsync.theme.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Segment
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.fiap.wtcsync.theme.themedDrawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.theme.NavBarActive
import br.com.fiap.wtcsync.theme.NavBarActiveFill
import br.com.fiap.wtcsync.theme.NavBarBackground
import br.com.fiap.wtcsync.theme.NavBarBorder
import br.com.fiap.wtcsync.theme.NavBarInactive

@Suppress("DEPRECATION")
enum class BottomNavTab(val icon: ImageVector, val label: String) {
    CLIENTES(Icons.Default.Group, "Clientes"),
    SEGMENTOS(Icons.Default.Segment, "Segmentos"),
    CAMPANHAS(Icons.Default.Campaign, "Campanhas"),
    CHAT(Icons.AutoMirrored.Filled.Chat, "Chat"),
    MEU_PERFIL(Icons.Default.Person, "Meu perfil"),
    ULTIMA_CAMPANHA(Icons.Default.Campaign, "Campanha"),
    OUTRAS(Icons.Default.MoreHoriz, "Outras")
}

@Composable
fun BottomNavBar(
    tabs: List<BottomNavTab> = listOf(BottomNavTab.CLIENTES, BottomNavTab.SEGMENTOS, BottomNavTab.CAMPANHAS, BottomNavTab.CHAT),
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .themedDrawBehind { palette ->
                drawLine(
                    color = palette.NavBarBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NavBarBackground,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
                    .height(68.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        isSelected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val containerColor by animateColorAsState(if (isSelected) NavBarActiveFill else Color.Transparent, tween(WtcMotion.SHORT), label = "navContainer")
    val contentColor by animateColorAsState(if (isSelected) NavBarActive else NavBarInactive, tween(WtcMotion.SHORT), label = "navContent")
    val shape = RoundedCornerShape(12.dp)

    Surface(
        onClick = onClick,
        modifier = modifier.pressScale(interaction).semantics { selected = isSelected; role = Role.Tab },
        interactionSource = interaction,
        shape = shape,
        color = containerColor,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        border = if (isSelected) BorderStroke(1.dp, NavBarBorder) else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tab.label,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}
