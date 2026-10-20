package br.com.fiap.wtcsync.data.repository

import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.data.remote.AuthApi
import br.com.fiap.wtcsync.data.remote.dto.LoginRequest
import br.com.fiap.wtcsync.data.remote.dto.LoginResponse
import br.com.fiap.wtcsync.data.remote.dto.RegisterRequest
import br.com.fiap.wtcsync.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {
    // Registration and password login must use the same identity store (the API).
    suspend fun registerWithEmail(email: String, password: String, name: String): Resource<User> = request {
        val response = authApi.register(RegisterRequest(email.trim(), password, name.trim()))
        Resource.Success(User(response.id, name, response.email, UserRole.CLIENTE))
    }

    private suspend fun establishSession(response: LoginResponse): Resource<User> {
        if (response.token.isBlank() || response.email.isBlank()) {
            return Resource.Error("A API retornou uma sessão inválida.")
        }
        // New servers return the verified profile with the session. Older servers keep the /me fallback.
        // Restoring an existing session still verifies it with /me before granting access.
        val profile = response.profile ?: authApi.me("Bearer ${response.token}")
        if (!profile.email.equals(response.email, ignoreCase = true)) {
            return Resource.Error("A API retornou um perfil diferente da conta autenticada.")
        }
        val role = profile.userRole()
            ?: return Resource.Error("Perfil não reconhecido pela API. Verifique a versão publicada do servidor.")
        sessionManager.saveSession(response.token, role.name, profile.email)
        return Resource.Success(User(profile.id ?: profile.email, profile.name ?: profile.email, profile.email, role))
    }

    suspend fun loginWithApi(email: String, password: String): Resource<User> = request {
        sessionManager.clear()
        establishSession(authApi.login(LoginRequest(email.trim(), password)))
    }

    suspend fun loginWithGoogle(idToken: String): Resource<User> = request {
        sessionManager.clear()
        val firebaseUser = firebaseAuth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).await().user ?: return@request Resource.Error("Conta Google indisponível.")
        val token = firebaseUser.getIdToken(false).await().token
            ?: return@request Resource.Error("Não foi possível obter o token do Firebase.")
        establishSession(authApi.loginGoogle(mapOf("idToken" to token)))
    }

    suspend fun restoreSession(): Resource<User>? {
        sessionManager.restoreSession()
        val token = sessionManager.currentToken ?: return null
        val email = sessionManager.currentEmail ?: run { sessionManager.clear(); return null }
        return request {
            try {
                establishSession(LoginResponse(token, email, 0L))
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) sessionManager.clear()
                throw e
            }
        }
    }

    fun signOutFirebase() = firebaseAuth.signOut()

    private suspend fun request(block: suspend () -> Resource<User>): Resource<User> = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Resource.Error(when (e.code()) {
            400 -> "Verifique os dados informados. O e-mail pode já estar cadastrado."
            401 -> "Não foi possível autenticar. Confira suas credenciais e tente novamente."
            403 -> "Conta sem acesso à API. Solicite a liberação do seu cadastro."
            404 -> "A versão publicada da API não oferece esta operação."
            409 -> "Este e-mail já está cadastrado."
            503 -> "Autenticação temporariamente indisponível no servidor. Tente novamente."
            else -> "Falha HTTP na autenticação (${e.code()})."
        })
    } catch (e: java.net.SocketTimeoutException) {
        Resource.Error("O servidor demorou para responder. Tente novamente.")
    } catch (e: java.io.IOException) {
        Resource.Error("Não foi possível conectar à API. Verifique sua conexão.")
    } catch (e: Exception) {
        Resource.Error("Falha na autenticação (${e.javaClass.simpleName}).")
    }
}
