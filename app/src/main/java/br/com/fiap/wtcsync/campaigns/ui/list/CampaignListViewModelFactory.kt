package br.com.fiap.wtcsync.campaigns.ui.list

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.fiap.wtcsync.WtcSyncApp

class CampaignListViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = application as WtcSyncApp
        @Suppress("UNCHECKED_CAST")
        return CampaignListViewModel(
            repository = app.campaignRepository
        ) as T
    }
}
