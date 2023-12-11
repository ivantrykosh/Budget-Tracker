package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TokenDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("register")
    suspend fun signUp(@Body request: AuthDto)

    @POST("login")
    suspend fun login(@Body request: AuthDto): TokenDto

    @GET("refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): TokenDto

    @POST("send-confirmation-email")
    suspend fun sendConfirmationEmail(@Body request: AuthDto)
}