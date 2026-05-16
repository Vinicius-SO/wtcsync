package br.com.fiap.wtcsync.campaigns.ui.detail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.fiap.wtcsync.WtcSyncApp

class CampaignDetailViewModelFactory(
    private val application: Application,
    private val campaignId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = application as WtcSyncApp
        @Suppress("UNCHECKED_CAST")
        return CampaignDetailViewModel(
            repository = app.campaignRepository,
            campaignId = campaignId
        ) as T
    }
}
