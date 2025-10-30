package br.com.fiap.wtcsync.data.firebase

import android.util.Log
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.data.model.enums.UserRole
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val TAG = "FirestoreDataSource"
    private val usersCollection = firestore.collection("users")

    init {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore.firestoreSettings = settings
            Log.d(TAG, "Firestore settings configured. persistenceEnabled=${settings.isPersistenceEnabled}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao configurar FirestoreSettings", e)
        }
    }

    suspend fun saveUser(user: User): Task<Void> {
        val data = mapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "role" to (user.role?.name ?: "CLIENTE")
        )
        Log.d(TAG, "Salvando usuário no Firestore: ${user.uid}")
        return usersCollection.document(user.uid).set(data)
    }

    suspend fun getUser(uid: String): User? {
        try {
            Log.d(TAG, "Buscando usuário no Firestore: $uid")

            // Primeiro tenta do servidor (online)
            val doc = try {
                usersCollection.document(uid).get(Source.SERVER).await()
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao buscar do servidor, tentando cache: ${e.message}")
                // Se falhar, tenta do cache
                usersCollection.document(uid).get(Source.CACHE).await()
            }

            return if (doc.exists()) {
                val roleString = doc.getString("role") ?: "CLIENTE"
                val role = try {
                    UserRole.valueOf(roleString)
                } catch (ex: Exception) {
                    Log.w(TAG, "Role inválida no documento ($uid): $roleString. Usando CLIENTE")
                    UserRole.CLIENTE
                }
                User(
                    uid = doc.getString("uid") ?: uid,
                    name = doc.getString("name"),
                    email = doc.getString("email"),
                    role = role
                ).also {
                    Log.d(TAG, "User encontrado no Firestore: $uid")
                }
            } else {
                Log.d(TAG, "Documento usuário não existe: $uid")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar usuário $uid: ${e.message}", e)
            throw e
        }
    }

    /**
     * Busca o usuário ou cria um novo se não existir
     * Este método garante que sempre haverá um usuário no Firestore após o login
     */
    suspend fun getOrCreateUser(uid: String, name: String?, email: String?): User {
        return try {
            Log.d(TAG, "getOrCreateUser iniciado para UID: $uid")

            // Tenta buscar usuário existente
            val existingUser = try {
                getUser(uid)
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao buscar usuário, será criado um novo: ${e.message}")
                null
            }

            if (existingUser != null) {
                Log.d(TAG, "Usuário já existe no Firestore: $uid")
                return existingUser
            }

            // Se não existir, cria um novo
            Log.d(TAG, "Usuário não existe, criando novo no Firestore: $uid")
            val newUser = User(
                uid = uid,
                name = name,
                email = email,
                role = UserRole.CLIENTE
            )

            // IMPORTANTE: Aguarda o salvamento completar
            saveUser(newUser).await()
            Log.d(TAG, "Novo usuário criado com sucesso no Firestore: $uid")

            newUser
        } catch (e: Exception) {
            Log.e(TAG, "Erro crítico ao buscar ou criar usuário $uid: ${e.message}", e)
            throw e
        }
    }
}