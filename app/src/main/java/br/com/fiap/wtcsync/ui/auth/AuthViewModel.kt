package br.com.fiap.wtcsync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.repository.AuthRepository
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.util.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager? = null
) : ViewModel() {
    private val _userState = MutableStateFlow<Resource<User>?>(Resource.Loading())
    val userState: StateFlow<Resource<User>?> = _userState
    private val _currentRole = MutableStateFlow<UserRole?>(null)
    val currentRole: StateFlow<UserRole?> = _currentRole
    private val _isRestoring = MutableStateFlow(true)
    val isRestoring: StateFlow<Boolean> = _isRestoring
    private var authJob: Job? = null

    private val restoreJob = viewModelScope.launch {
        try {
            val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) { repository.restoreSession() }
            if (result is Resource.Success) _currentRole.value = result.data?.role
            _userState.value = result
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _userState.value = Resource.Error("Não foi possível restaurar sua sessão. Entre novamente.")
        } finally { _isRestoring.value = false }
    }

    fun useManualLogin() {
        if (authJob?.isActive == true) return
        restoreJob.cancel()
        _isRestoring.value = false
        _currentRole.value = null
        _userState.value = null
    }

    private fun authenticate(block: suspend () -> Resource<User>) {
        if (authJob?.isActive == true) return
        _userState.value = Resource.Loading()
        authJob = viewModelScope.launch {
            // An explicit login replaces the old restore request; do not queue behind a slow /me.
            restoreJob.cancel()
            restoreJob.join()
            _userState.value = Resource.Loading()
            _currentRole.value = null
            try {
                val result = withTimeoutOrNull(AUTH_TIMEOUT_MS) { block() }
                    ?: Resource.Error("O servidor demorou para responder. Tente novamente.")
                if (result is Resource.Success) _currentRole.value = result.data?.role
                _userState.value = result
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _userState.value = Resource.Error("Não foi possível autenticar. Tente novamente.")
            }
        }
    }

    fun login(email: String, password: String) = authenticate {
        if (email.isBlank() || password.isBlank()) Resource.Error("Preencha e-mail e senha.")
        else repository.loginWithApi(email, password)
    }

    // Account privileges are assigned by the server; the UI selection cannot grant access.
    fun register(email: String, password: String, name: String, role: UserRole) = authenticate {
        repository.registerWithEmail(email, password, name)
    }

    fun loginWithGoogle(idToken: String) = authenticate { repository.loginWithGoogle(idToken) }

    fun logout() {
        authJob?.cancel()
        restoreJob.cancel()
        _currentRole.value = null
        _userState.value = null
        authJob = viewModelScope.launch {
            sessionManager?.clear()
            repository.signOutFirebase()
            _currentRole.value = null
            _userState.value = null
        }
    }

    fun resetState() { _userState.value = null }

    companion object {
        // Covers Firebase authentication and the API login + profile requests.
        private const val AUTH_TIMEOUT_MS = 180_000L
    }
}
