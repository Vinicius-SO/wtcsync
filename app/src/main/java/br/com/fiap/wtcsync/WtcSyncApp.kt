package br.com.fiap.wtcsync

import android.app.Application
import br.com.fiap.wtcsync.campaigns.data.CampaignRepository
import br.com.fiap.wtcsync.data.firebase.FirestoreDataSource
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.remote.ApiService
import br.com.fiap.wtcsync.data.repository.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class WtcSyncApp : Application() {

    lateinit var authRepository: AuthRepository
        private set
    lateinit var sessionManager: SessionManager
        private set
    lateinit var apiService: ApiService
        private set
    lateinit var campaignRepository: CampaignRepository
        private set

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestore.firestoreSettings = settings
        FirebaseFirestore.setLoggingEnabled(true)

        val firestoreDataSource = FirestoreDataSource(firestore)

        sessionManager = SessionManager(this)
        apiService = ApiService(sessionManager)
        campaignRepository = CampaignRepository(apiService.campaignApi)
        authRepository = AuthRepository(
            firebaseAuth = firebaseAuth,
            firestoreDataSource = firestoreDataSource,
            authApi = apiService.authApi,
            sessionManager = sessionManager
        )
    }
}
