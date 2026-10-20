package br.com.fiap.wtcsync

import androidx.lifecycle.ViewModelStore
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.remote.AuthApi
import br.com.fiap.wtcsync.data.remote.dto.*
import br.com.fiap.wtcsync.data.repository.AuthRepository
import br.com.fiap.wtcsync.ui.auth.AuthViewModel
import br.com.fiap.wtcsync.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelLatencyTest {
    private val store = ViewModelStore()
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }
    @Test fun explicitLoginCancelsAnUnresponsiveRestoreAndDoesNotWaitForItsTimeout() = runTest {
        val pending = CompletableDeferred<Unit>()
        var oldRequestCancelled = false
        var loginCalls = 0
        var profileCalls = 0
        val api = object : AuthApi {
            override suspend fun me(authorization: String): AuthProfile {
                profileCalls++
                try { pending.await() } finally { oldRequestCancelled = true }
                return AuthProfile("old@example.invalid", "CLIENT")
            }
            override suspend fun login(request: LoginRequest): LoginResponse {
                loginCalls++
                return LoginResponse("new-token", request.email, 86400000L,
                    AuthProfile(request.email, "OPERATOR", "new-user", "New user"))
            }
            override suspend fun loginGoogle(request: Map<String, String>): LoginResponse = error("unused")
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("unused")
        }
        val manager = mock(SessionManager::class.java)
        `when`(manager.currentToken).thenReturn("old-token")
        `when`(manager.currentEmail).thenReturn("old@example.invalid")
        val model = AuthViewModel(AuthRepository(mock(FirebaseAuth::class.java), api, manager), manager)
        store.put("auth", model)
        runCurrent()
        assertEquals(1, profileCalls)
        assertEquals(0, loginCalls)
        model.login("new@example.invalid", "password")
        runCurrent()
        assertTrue(oldRequestCancelled)
        assertEquals(1, loginCalls)
        assertTrue(model.userState.value is Resource.Success)
        assertEquals("new@example.invalid", model.userState.value?.data?.email)
        assertEquals(0L, testScheduler.currentTime) // no three-minute restore timeout elapsed
        pending.complete(Unit)
        runCurrent()
        assertEquals("new@example.invalid", model.userState.value?.data?.email)
        verify(manager).saveSession("new-token", "OPERATOR", "new@example.invalid")
    }
}
