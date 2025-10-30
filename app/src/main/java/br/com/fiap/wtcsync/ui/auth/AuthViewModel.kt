package br.com.fiap.wtcsync.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.repository.AuthRepository
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val TAG = "AuthViewModel"

    private val _userState = MutableStateFlow<Resource<User>?>(null)
    val userState: StateFlow<Resource<User>?> = _userState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando login com email")
                _userState.value = Resource.Loading()
                val result = repository.loginWithEmail(email, password)
                _userState.value = result
                Log.d(TAG, "Login resultado: ${result.javaClass.simpleName}")
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

    /**
     * Reseta o estado para null, permitindo novas operações
     */
    fun resetState() {
        Log.d(TAG, "Resetando estado do ViewModel")
        _userState.value = null
    }
}