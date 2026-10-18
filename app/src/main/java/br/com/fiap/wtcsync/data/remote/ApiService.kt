package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.campaigns.data.CampaignApi
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.segments.data.dto.SegmentApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = sessionManager.currentToken
        val request = if (token != null && chain.request().header("Authorization") == null &&
            chain.request().url.encodedPath !in setOf("/api/auth/login", "/api/auth/google", "/api/auth/register")) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

class ApiService(private val sessionManager: SessionManager) {

    companion object {
        private const val BASE_URL = "https://wtcsync-api.onrender.com/"
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .callTimeout(150, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val campaignApi: CampaignApi by lazy {
        retrofit.create(CampaignApi::class.java)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val clientApi: ClientApi by lazy {
        retrofit.create(ClientApi::class.java)
    }

    val messageApi: MessageApi by lazy {
        retrofit.create(MessageApi::class.java)
    }

    val segmentApi: SegmentApi by lazy {
        retrofit.create(SegmentApi::class.java)
    }
}