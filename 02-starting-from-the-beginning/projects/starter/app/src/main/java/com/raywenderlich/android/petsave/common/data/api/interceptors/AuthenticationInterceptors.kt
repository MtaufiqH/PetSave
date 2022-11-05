package com.raywenderlich.android.petsave.common.data.api.interceptors

import android.os.Build
import androidx.annotation.RequiresApi
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.AUTH_HEADER
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.TOKEN_TYPE
import com.raywenderlich.android.petsave.common.data.preferences.PetSavePreferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.time.Instant
import javax.inject.Inject

class AuthenticationInterceptors @Inject constructor(
    private val preferences: PetSavePreferences
) : Interceptor {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun intercept(chain: Interceptor.Chain): Response {
        val currentToken = preferences.getToken()
        val tokenExpireTime = Instant.ofEpochSecond(preferences.getTokenExpirationTime())
        val request = chain.request()

        val interceptedRequest: Request
        if (tokenExpireTime.isAfter(Instant.now())) {
            // still valid token
            interceptedRequest = chain.createAuthRequest(currentToken)
        } else {
            // token expired, request new.
            val tokenRefresh = chain.refreshToken()
        }

    }
}

private fun Interceptor.Chain.refreshToken(): Response {

}

private fun Interceptor.Chain.createAuthRequest(currentToken: String): Request {
    return request().newBuilder().addHeader(AUTH_HEADER, TOKEN_TYPE + currentToken).build()
}
