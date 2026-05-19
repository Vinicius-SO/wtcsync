package br.com.fiap.wtcsync.segments.ui.create

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.fiap.wtcsync.WtcSyncApp

class CreateSegmentViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = application as WtcSyncApp
        @Suppress("UNCHECKED_CAST")
        return CreateSegmentViewModel(
            repository = app.segmentRepository
        ) as T
    }
}