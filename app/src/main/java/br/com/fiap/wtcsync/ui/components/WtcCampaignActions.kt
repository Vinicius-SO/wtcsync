package br.com.fiap.wtcsync.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcsync.campaigns.domain.Campaign

@Composable
fun WtcCampaignActions(campaign: Campaign) {
    val openUrl = rememberWtcOpenUrl()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        campaign.actions.forEach { action ->
            val url = campaign.actionUrls[action.action]
            WtcButton(onClick = { url?.let(openUrl) },
                enabled = !url.isNullOrBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(action.title)
            }
        }
    }
}

@Composable
fun rememberWtcOpenUrl(): (String) -> Unit {
    val context = LocalContext.current
    return { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
