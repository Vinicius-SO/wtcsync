package br.com.fiap.wtcsync.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.repository.AuthRepository
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager? = null
) : ViewModel() {

    private val TAG = "AuthViewModel"

    private val _userState = MutableStateFlow<Resource<User>?>(null)
    val userState: StateFlow<Resource<User>?> = _userState

    private val _currentRole = MutableStateFlow<UserRole?>(null)
    val currentRole: StateFlow<UserRole?> = _currentRole

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            sessionManager?.let { manager ->
                manager.restoreSession()
                val role = manager.currentRole
                _currentRole.value = when (role?.uppercase()) {
                    "OPERATOR" -> UserRole.OPERATOR
                    "ATENDENTE" -> UserRole.ATENDENTE
                    else -> UserRole.CLIENTE
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando login com email")
                _userState.value = Resource.Loading()
                val result = repository.loginWithApi(email, password)
                if (result is Resource.Success) {
                    val role = result.data?.role
                    Log.d(TAG, "Login bem-sucedido. Role do usuário: $role")
                    _currentRole.value = role
                } else if (result is Resource.Error) {
                    Log.w(TAG, "Login falhou: ${result.message}")
                }
                _userState.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Erro no login", e)
                _userState.value = Resource.Error(e.message ?: "Erro desconhecido no login")
            }
        }
    }

    fun register(email: String, password: String, name: String, role: UserRole) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando registro")
                _userState.value = Resource.Loading()
                val result = repository.registerWithEmail(email, password, name, role)
                _userState.value = result
                Log.d(TAG, "Registro resultado: ${result.javaClass.simpleName}")
            } catch (e: Exception) {
                Log.e(TAG, "Erro no registro", e)
                _userState.value = Resource.Error(e.message ?: "Erro desconhecido no registro")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando login com Google")
                _userState.value = Resource.Loading()
                val result = repository.loginWithGoogle(idToken)
                _userState.value = result
                Log.d(TAG, "Login Google resultado: ${result.javaClass.simpleName}")
            } catch (e: Exception) {
                Log.e(TAG, "Erro no login com Google", e)
                _userState.value = Resource.Error(e.message ?: "Erro desconhecido no login com Google")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager?.clear()
            _currentRole.value = null
            _userState.value = null
        }
    }

    fun resetState() {
        Log.d(TAG, "Resetando estado do ViewModel")
        _userState.value = null
    }
}
