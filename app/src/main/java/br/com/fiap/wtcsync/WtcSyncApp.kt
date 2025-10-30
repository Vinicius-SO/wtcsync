package br.com.fiap.wtcsync

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import br.com.fiap.wtcsync.data.firebase.FirestoreDataSource
import br.com.fiap.wtcsync.data.repository.AuthRepository

class WtcSyncApp : Application() {

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Inicializa o Firebase o mais cedo possível
        FirebaseApp.initializeApp(this)

        // Instâncias globais
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // CONFIGURAÇÕES DO FIRESTORE
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Habilita cache offline
            .build()

        firestore.firestoreSettings = settings

        // Habilita logs do Firestore (opcional, útil para debug)
        FirebaseFirestore.setLoggingEnabled(true)

        // Força a ativação da rede
        firestore.enableNetwork()
            .addOnSuccessListener {
                Log.d("WtcSyncApp", "Firestore network enabled successfully")
            }
            .addOnFailureListener { e ->
                Log.e("WtcSyncApp", "Failed to enable Firestore network", e)
            }

        // DataSource e Repository
        val firestoreDataSource = FirestoreDataSource(firestore)
        authRepository = AuthRepository(firebaseAuth, firestoreDataSource)
    }
}