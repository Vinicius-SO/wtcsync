package br.com.fiap.wtcsync

import br.com.fiap.wtcsync.data.remote.dto.LoginResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LoginContractTest {
    @Test fun swaggerResponseParsesNumericExpiration() {
        val body = """{"token":"header.payload.signature","email":"tester@example.com","expiresIn":86400000}"""
        val response = Gson().fromJson(body, LoginResponse::class.java)
        assertNotNull(response.token)
        assertEquals("tester@example.com", response.email)
        assertEquals(86400000L, response.expiresIn)
    }
}
