package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TokenDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto

interface AuthRepository {

    suspend fun signUp(request: AuthDto)

    suspend fun login(request: AuthDto): TokenDto

    suspend fun refreshToken(token: String): TokenDto

    suspend fun sendConfirmationEmail(request: AuthDto)
}