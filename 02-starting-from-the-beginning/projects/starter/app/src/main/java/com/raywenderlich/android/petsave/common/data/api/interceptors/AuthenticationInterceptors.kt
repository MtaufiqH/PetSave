package com.raywenderlich.android.petsave.common.data.api.interceptors

import android.os.Build
import androidx.annotation.RequiresApi
import com.raywenderlich.android.petsave.common.data.api.ApiConstants.AUTH_ENDPOINT
import com.raywenderlich.android.petsave.common.data.api.ApiConstants.KEY
import com.raywenderlich.android.petsave.common.data.api.ApiConstants.SECRET
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.AUTH_HEADER
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.CLIENT_ID
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.CLIENT_SECRET
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.GRANT_TYPE_KEY
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.GRANT_TYPE_VALUE
import com.raywenderlich.android.petsave.common.data.api.ApiParameters.TOKEN_TYPE
import com.raywenderlich.android.petsave.common.data.api.model.ApiToken
import com.raywenderlich.android.petsave.common.data.preferences.PetSavePreferences
import com.squareup.moshi.Moshi
import okhttp3.FormBody
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
            // still valid token, proceed request
            interceptedRequest = chain.createAuthRequest(currentToken)
        } else {
            // token expired, request new/refresh.
            val tokenRefresh = chain.refreshToken()
            interceptedRequest = if (tokenRefresh.isSuccessful) {
                val newToken = mapToken(tokenRefresh)
                if (newToken.isValid()) {
                    storeNewToken(newToken)
                    chain.createAuthRequest(newToken.accessToken.orEmpty())
                } else {
                    request
                }
            } else {
                request
            }
        }

        return chain.proceedDeletingTokenIfUnAuthorized(interceptedRequest)

    }

    private fun storeNewToken(newToken: ApiToken) {
        preferences.apply {
            putToken(newToken.accessToken.orEmpty())
            putTokenType(newToken.tokenType.orEmpty())
            putTokenExpirationTime(newToken.expiresAt)
        }
    }

    private fun mapToken(tokenRefresh: Response): ApiToken {
        val moshi = Moshi.Builder().build()
        val tokenAdapter = moshi.adapter<ApiToken>(ApiToken::class.java)
        val responseBody = tokenRefresh.body

        return tokenAdapter.fromJson(responseBody?.string()) ?: ApiToken.INVALID

    }


    companion object {
        const val UNAUTHORIZED = 401
    }

    private fun Interceptor.Chain.proceedDeletingTokenIfUnAuthorized(interceptedRequest: Request): Response {
        val response = proceed(interceptedRequest)
        if (response.code == UNAUTHORIZED) {
            preferences.deleteTokenInfo()
        }
        return response
    }

    private fun Interceptor.Chain.refreshToken(): Response {
        val url = request()
            .url
            .newBuilder(AUTH_ENDPOINT)!!
            .build()

        val body =
            FormBody.Builder().add(GRANT_TYPE_KEY, GRANT_TYPE_VALUE).add(CLIENT_ID, KEY)
                .add(CLIENT_SECRET, SECRET).build()

        val tokenRefresh = request()
            .newBuilder()
            .post(body)
            .url(url)
            .build()

        return proceedDeletingTokenIfUnAuthorized(tokenRefresh)
    }

    private fun Interceptor.Chain.createAuthRequest(currentToken: String): Request {
        return request().newBuilder().addHeader(AUTH_HEADER, TOKEN_TYPE + currentToken).build()
    }
}




