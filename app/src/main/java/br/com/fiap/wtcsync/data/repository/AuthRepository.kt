package br.com.fiap.wtcsync.data.repository

import android.util.Log
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.data.firebase.FirestoreDataSource
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreDataSource: FirestoreDataSource
) {
    private val TAG = "AuthRepository"

    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Erro ao criar usuário")
            val user = User(firebaseUser.uid, name, firebaseUser.email, role)
            firestoreDataSource.saveUser(user).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cadastrar", e)
            Resource.Error(e.message ?: "Erro ao cadastrar")
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Usuário não encontrado")

            // Busca ou cria o usuário no Firestore
            val user = try {
                firestoreDataSource.getUser(firebaseUser.uid)
                    ?: User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email, UserRole.CLIENTE).also {
                        firestoreDataSource.saveUser(it).await()
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao buscar usuário do Firestore, usando dados do Firebase Auth", e)
                User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email, UserRole.CLIENTE)
            }

            Resource.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer login", e)
            Resource.Error(e.message ?: "Erro ao fazer login")
        }
    }

    suspend fun loginWithGoogle(idToken: String): Resource<User> {
        return try {
            Log.d(TAG, "Iniciando login com Google")

            // 1. Autentica com Firebase Auth
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Erro ao autenticar com Google")

            Log.d(TAG, "Autenticação Firebase bem-sucedida. UID: ${firebaseUser.uid}")

            // 2. Busca ou cria o usuário no Firestore usando o método getOrCreateUser
            val user = firestoreDataSource.getOrCreateUser(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName,
                email = firebaseUser.email
            )

            Log.d(TAG, "Login com Google concluído com sucesso")
            Resource.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Erro no login com Google", e)
            Resource.Error(e.message ?: "Erro no login com Google")
        }
    }
}