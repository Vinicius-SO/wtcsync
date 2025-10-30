package br.com.fiap.wtcsync.ui.auth

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

    private val _userState = MutableStateFlow<Resource<User>?>(null)
    val userState: StateFlow<Resource<User>?> = _userState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            _userState.value = repository.loginWithEmail(email, password)
        }
    }

    fun register(email: String, password: String,name: String, role: UserRole) {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            _userState.value = repository.registerWithEmail(email, password, name, role)
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            _userState.value = repository.loginWithGoogle(idToken)
        }
    }
}