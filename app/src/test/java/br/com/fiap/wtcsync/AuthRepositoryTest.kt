package br.com.fiap.wtcsync

import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.data.remote.AuthApi
import br.com.fiap.wtcsync.data.remote.dto.*
import br.com.fiap.wtcsync.data.repository.AuthRepository
import br.com.fiap.wtcsync.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class AuthRepositoryTest {
    private class FakeApi : AuthApi {
        var profile = AuthProfile("test@example.com", "OPERATOR", "user-id", "Test")
        var profileFailure: Exception? = null
        var loginProfile: AuthProfile? = null
        var profileCalls = 0
        var registered: RegisterRequest? = null
        var authorization: String? = null
        override suspend fun login(request: LoginRequest) = LoginResponse("api-jwt", request.email, 86400000L, loginProfile)
        override suspend fun loginGoogle(request: Map<String, String>) = error("not used")
        override suspend fun me(authorization: String): AuthProfile {
            profileCalls++
            this.authorization = authorization
            profileFailure?.let { throw it }
            return profile
        }
        override suspend fun register(request: RegisterRequest): RegisterResponse {
            registered = request
            return RegisterResponse("new-id", request.email)
        }
    }

    @Test fun loginPersistsOnlyConfirmedProfileAndUsesItsId() = runBlocking {
        val api = FakeApi()
        val manager = mock(SessionManager::class.java)
        val repository = AuthRepository(mock(FirebaseAuth::class.java), api, manager)
        val result = repository.loginWithApi(" test@example.com ", "password")
        assertTrue(result is Resource.Success)
        assertEquals(UserRole.OPERATOR, result.data?.role)
        assertEquals("user-id", result.data?.uid)
        assertEquals("Bearer api-jwt", api.authorization)
        verify(manager).saveSession("api-jwt", "OPERATOR", "test@example.com")
    }

    @Test fun legacyServerWithoutRoleCannotEstablishSession() = runBlocking {
        val api = FakeApi().apply { profile = AuthProfile("test@example.com", null) }
        val manager = mock(SessionManager::class.java)
        val result = AuthRepository(mock(FirebaseAuth::class.java), api, manager)
            .loginWithApi("test@example.com", "password")
        assertTrue(result is Resource.Error)
        verify(manager).clear()
        verifyNoMoreInteractions(manager)
    }

    @Test fun verifiedProfileInLoginAvoidsTheSecondNetworkRequest() = runBlocking {
        val api = FakeApi().apply { loginProfile = profile; profileFailure = java.io.IOException("unnecessary call") }
        val manager = mock(SessionManager::class.java)
        val result = AuthRepository(mock(FirebaseAuth::class.java), api, manager)
            .loginWithApi("test@example.com", "password")
        assertTrue(result is Resource.Success)
        assertEquals("user-id", result.data?.uid)
        assertEquals(0, api.profileCalls)
        verify(manager).saveSession("api-jwt", "OPERATOR", "test@example.com")
    }

    @Test fun embeddedProfileForAnotherAccountIsRejectedWithoutFallbackOrSessionWrite() = runBlocking {
        val api = FakeApi().apply { loginProfile = AuthProfile("other@example.com", "OPERATOR") }
        val manager = mock(SessionManager::class.java)
        val result = AuthRepository(mock(FirebaseAuth::class.java), api, manager).loginWithApi("test@example.com", "password")
        assertTrue(result is Resource.Error)
        assertEquals(0, api.profileCalls)
        verify(manager).clear(); verifyNoMoreInteractions(manager)
    }

    @Test fun unknownEmbeddedRoleIsRejectedWithoutTrustingTheUi() = runBlocking {
        val api = FakeApi().apply { loginProfile = AuthProfile("test@example.com", "UNKNOWN") }
        val manager = mock(SessionManager::class.java)
        val result = AuthRepository(mock(FirebaseAuth::class.java), api, manager).loginWithApi("test@example.com", "password")
        assertTrue(result is Resource.Error)
        assertEquals(0, api.profileCalls)
        verify(manager).clear(); verifyNoMoreInteractions(manager)
    }

    @Test fun restoringSavedSessionStillChecksTheAccountWithTheServer() = runBlocking {
        val api = FakeApi()
        val manager = mock(SessionManager::class.java)
        `when`(manager.currentToken).thenReturn("api-jwt")
        `when`(manager.currentEmail).thenReturn("test@example.com")
        val result = AuthRepository(mock(FirebaseAuth::class.java), api, manager).restoreSession()
        assertTrue(result is Resource.Success)
        assertEquals(1, api.profileCalls)
        assertEquals("Bearer api-jwt", api.authorization)
    }

    @Test fun registrationUsesBackendWithoutCreatingFirebaseIdentity() = runBlocking {
        val api = FakeApi()
        val firebase = mock(FirebaseAuth::class.java)
        val manager = mock(SessionManager::class.java)
        val result = AuthRepository(firebase, api, manager)
            .registerWithEmail(" test@example.com ", "password", "Test")
        assertTrue(result is Resource.Success)
        assertEquals("test@example.com", api.registered?.email)
        verifyNoInteractions(firebase, manager)
    }

    @Test fun cancellationPropagatesInsteadOfBecomingAnAuthenticationError() = runBlocking {
        val api = FakeApi().apply { profileFailure = CancellationException("cancelled") }
        val repository = AuthRepository(mock(FirebaseAuth::class.java), api, mock(SessionManager::class.java))
        try {
            repository.loginWithApi("test@example.com", "password")
            fail("Expected cancellation")
        } catch (_: CancellationException) { }
    }

    @Test fun profileForAnotherAccountCannotEstablishSession() = runBlocking {
        val api = FakeApi().apply { profile = AuthProfile("other@example.com", "ADMIN") }
        val manager = mock(SessionManager::class.java)
        val result = AuthRepository(mock(FirebaseAuth::class.java), api, manager)
            .loginWithApi("test@example.com", "password")
        assertTrue(result is Resource.Error)
        verify(manager).clear()
        verifyNoMoreInteractions(manager)
    }
}
