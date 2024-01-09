package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TokenDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Api for auth
 */
interface AuthApi {

    /**
     * Sign up with request
     *
     * @param request AuthDto request
     */
    @POST("register")
    suspend fun signUp(@Body request: AuthDto)

    /**
     * Login with request
     *
     * @param request AuthDto request
     */
    @POST("login")
    suspend fun login(@Body request: AuthDto): TokenDto

    /**
     * Refresh token with token
     *
     * @param token user's JWT
     */
    @GET("refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): TokenDto

    /**
     * Send confirmation email with request
     *
     * @param request AuthDto request
     */
    @POST("send-confirmation-email")
    suspend fun sendConfirmationEmail(@Body request: AuthDto)
}