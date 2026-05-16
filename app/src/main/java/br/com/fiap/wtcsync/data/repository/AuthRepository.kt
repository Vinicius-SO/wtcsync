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

    suspend fun loginWithApi(email: String, password: String): Resource<User> {
        return try {
            val api = authApi ?: return Resource.Error("API não configurada")
            val manager = sessionManager ?: return Resource.Error("SessionManager não configurado")

            val response = api.login(LoginRequest(email, password))
            val decodedRole = manager.decodeRoleFromJwt(response.token)
            val role = decodedRole?.takeIf { it.isNotBlank() }
                ?: inferRoleFromEmail(response.email)
            val uid = extractSubjectFromJwt(response.token) ?: response.email
            val userRole = when (role.uppercase()) {
                "OPERATOR" -> UserRole.OPERATOR
                "ATENDENTE" -> UserRole.ATENDENTE
                else -> UserRole.CLIENTE
            }
            Log.d(TAG, "Role decodificada: '$decodedRole' → final: '$role' → UserRole: $userRole")

            manager.saveSession(
                token = response.token,
                role = role,
                email = response.email
            )

            val user = User(
                uid = uid,
                name = response.email,
                email = response.email,
                role = userRole
            )

            Log.d(TAG, "Login API bem-sucedido. Role: $role")
            Resource.Success(user)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "Erro HTTP no login via API", e)
            val message = when (e.code()) {
                401 -> "Credenciais inválidas"
                403 -> "Acesso não autorizado"
                500 -> "Erro interno do servidor. Tente novamente."
                else -> "Erro na conexão (${e.code()})"
            }
            Resource.Error(message)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Erro de rede no login via API", e)
            Resource.Error("Sem conexão com a internet")
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout no login via API", e)
            Resource.Error("Tempo limite excedido. Verifique sua conexão.")
        } catch (e: Exception) {
            Log.e(TAG, "Erro no login via API", e)
            Resource.Error(e.message ?: "Erro ao fazer login via API")
        }
    }

    private fun inferRoleFromEmail(email: String): String {
        return when {
            email.contains("admin") -> "OPERATOR"
            else -> "CLIENTE"
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