package br.com.fiap.wtcsync.data.repository

import android.util.Log
import br.com.fiap.wtcsync.data.firebase.FirestoreDataSource
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.data.remote.AuthApi
import br.com.fiap.wtcsync.data.remote.dto.LoginRequest
import br.com.fiap.wtcsync.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthException

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreDataSource: FirestoreDataSource,
    private val authApi: AuthApi? = null,
    private val sessionManager: SessionManager? = null
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

            // Se a criação do usuário no Firebase for bem-sucedida, salva no Firestore
            val user = User(firebaseUser.uid, name, firebaseUser.email, role)
            firestoreDataSource.saveUser(user).await()

            // Se tudo der certo, retorna Sucesso
            Resource.Success(user)

        } catch (e: FirebaseAuthException) {
            // 🚨 NOVO TRATAMENTO: Captura exceções específicas do Firebase
            Log.e(TAG, "Erro FirebaseAuth ao cadastrar", e)

            // Mapeamento de códigos de erro para mensagens amigáveis
            val friendlyMessage = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está cadastrado."
                "ERROR_WEAK_PASSWORD" -> "A senha deve ter pelo menos 6 caracteres."
                "ERROR_INVALID_EMAIL" -> "O formato do e-mail é inválido."
                else -> "Erro no cadastro. Tente novamente."
            }

            Resource.Error(friendlyMessage)

        } catch (e: Exception) {
            // Tratamento para outras exceções (rede, firestore, etc.)
            Log.e(TAG, "Erro geral ao cadastrar", e)
            // O `e.message` aqui pode ser nulo em alguns casos, use o Elvis operator `?:`
            Resource.Error(e.message ?: "Erro desconhecido ao cadastrar. Verifique sua conexão.")
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

    private suspend fun establishSession(response: br.com.fiap.wtcsync.data.remote.dto.LoginResponse): Resource<User> {
        val api = authApi ?: return Resource.Error("API não configurada")
        val manager = sessionManager ?: return Resource.Error("Sessão indisponível")
        if (response.token.isBlank() || response.email.isBlank()) return Resource.Error("Resposta inválida da API")
        manager.saveSession(response.token, "CLIENTE", response.email)
        return try {
            val profile = api.me()
            val roleName = profile["role"]?.uppercase() ?: ""
            val role = when (roleName) {
                "ADMIN", "OPERATOR", "ROLE_ADMIN", "ROLE_OPERATOR" -> UserRole.OPERATOR
                "ATENDENTE", "ROLE_ATENDENTE" -> UserRole.ATENDENTE
                "CLIENT", "CLIENTE", "ROLE_CLIENT", "ROLE_CLIENTE" -> UserRole.CLIENTE
                else -> {
                    manager.clear()
                    return Resource.Error("Perfil não reconhecido pela API: $roleName")
                }
            }
            manager.saveSession(response.token, role.name, response.email)
            Resource.Success(User(response.email, response.email, response.email, role))
        } catch (e: Exception) {
            manager.clear()
            Resource.Error("Login autenticado, mas não foi possível confirmar seu perfil.")
        }
    }

    suspend fun loginWithApi(email: String, password: String): Resource<User> {
        return try {
        val api = authApi ?: return Resource.Error("API não configurada")
        establishSession(api.login(LoginRequest(email, password)))
    } catch (e: retrofit2.HttpException) {
        Resource.Error(if (e.code() == 401) "E-mail ou senha inválidos." else "Falha HTTP no login (${e.code()}).")
    } catch (e: Exception) {
        Resource.Error("Não foi possível conectar à API: ${e.javaClass.simpleName}")
    }

    }

    private fun extractSubjectFromJwt(token: String): String? {
        return try {
            val payload = token.split(".")[1]
            val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
            org.json.JSONObject(decoded).optString("sub").ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loginWithGoogle(idToken: String): Resource<User> {
        return try {
        val firebaseUser = firebaseAuth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).await().user ?: return Resource.Error("Conta Google indisponível")
        val googleToken = firebaseUser.getIdToken(true).await().token
            ?: return Resource.Error("Não foi possível obter o token do Firebase")
        val api = authApi ?: return Resource.Error("API não configurada")
        establishSession(api.loginGoogle(mapOf("idToken" to googleToken)))
    } catch (e: retrofit2.HttpException) {
        Resource.Error(when (e.code()) {
            403 -> "Conta Google não cadastrada na API. Cadastre-se primeiro."
            401 -> "Não foi possível validar a conta Google."
            404 -> "O servidor ainda não oferece login Google. Atualize a API."
            else -> "Erro HTTP no login Google (${e.code()})."
        })
    } catch (e: Exception) {
        Resource.Error("Falha no Google: ${e.javaClass.simpleName}")
    }
    }
}
