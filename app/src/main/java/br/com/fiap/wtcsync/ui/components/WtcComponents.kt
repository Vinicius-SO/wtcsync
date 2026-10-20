package br.com.fiap.wtcsync.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.theme.*

enum class WtcButtonKind { Primary, Secondary, Text, DangerText }

/** Shared action sizing, shape, typography, colors and disabled/loading behavior. */
@Composable
fun WtcButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    kind: WtcButtonKind = WtcButtonKind.Primary,
    content: @Composable RowScope.() -> Unit
) {
    val foreground = when (kind) {
        WtcButtonKind.Primary -> YellowText
        WtcButtonKind.DangerText -> ErrorText
        else -> TextPrimary
    }
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = if (kind == WtcButtonKind.Primary) YellowBadge else androidx.compose.ui.graphics.Color.Transparent,
        contentColor = foreground,
        disabledContainerColor = CardGrayBg,
        disabledContentColor = TextSecondary
    )
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors,
        border = if (kind == WtcButtonKind.Secondary) BorderStroke(1.dp, BorderColor) else null,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                Spacer(Modifier.width(8.dp))
            }
            content()
        }
    }
}

@Composable
fun WtcCreateButton(onClick: () -> Unit) {
    WtcButton(onClick) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text("Criar")
    }
}

/** Screen title is shown once; forms put navigation/actions above it to avoid cramped toolbars. */
@Composable
fun WtcScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Voltar",
    onTitleClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = HeaderBg, modifier = modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    Box(Modifier.weight(1f)) {
                        WtcButton(onBack, kind = WtcButtonKind.Text) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(backLabel)
                        }
                    }
                } else {
                    HeaderTitle(title, subtitle, onTitleClick, leading, Modifier.weight(1f))
                }
                actions()
            }
            if (onBack != null) {
                HeaderTitle(title, subtitle, onTitleClick, leading, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
                Spacer(Modifier.height(4.dp))
            }
            HorizontalDivider(color = ListBorderLight)
        }
    }
}

@Composable
private fun HeaderTitle(title: String, subtitle: String?, onClick: (() -> Unit)?, leading: @Composable () -> Unit, modifier: Modifier) {
    Row(modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier), verticalAlignment = Alignment.CenterVertically) {
        leading()
        Column(Modifier.padding(horizontal = 4.dp).weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = TextSecondary, fontSize = 12.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun WtcAccountBar(label: String, onLogout: () -> Unit) {
    Surface(color = HeaderBg) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            WtcButton(onLogout, kind = WtcButtonKind.Text) { Text("Sair") }
        }
    }
}

@Composable
fun WtcStatePanel(title: String, message: String? = null, onRetry: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null, modifier: Modifier = Modifier, action: @Composable () -> Unit = {}) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        if (!message.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            WtcButton(onRetry) { Text("Tentar novamente") }
        }
        if (onBack != null) WtcButton(onBack, kind = WtcButtonKind.Text) { Text("Voltar") }
        action()
    }
}

@Composable
fun WtcFeedback(message: String, isError: Boolean = true, onDismiss: (() -> Unit)? = null) {
    Surface(color = if (isError) ErrorBg else SuccessBg, shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, color = if (isError) ErrorText else SuccessText, fontSize = 14.sp, modifier = Modifier.weight(1f))
            if (onDismiss != null) WtcButton(onDismiss, kind = WtcButtonKind.Text) { Text("Fechar") }
        }
    }
}

@Composable
fun WtcFilterChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(24.dp),
        color = if (selected) YellowBadge else CardSurface,
        border = BorderStroke(1.dp, if (selected) BorderColor else ListBorderLight)) {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) YellowText else TextSecondary, fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
