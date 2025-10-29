package br.com.fiap.wtcsync.data.repository


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
            firestoreDataSource.saveUser(user)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao cadastrar")
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Usuário não encontrado")
            val user = firestoreDataSource.getUser(firebaseUser.uid)
                ?: User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao fazer login")
        }
    }

    suspend fun loginWithGoogle(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Erro ao autenticar com Google")

            // Busca no Firestore ou cria padrão como CLIENTE
            val user = firestoreDataSource.getUser(firebaseUser.uid)
                ?: User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email, UserRole.CLIENTE).also {
                    firestoreDataSource.saveUser(it)
                }

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro no login com Google")
        }
    }
}