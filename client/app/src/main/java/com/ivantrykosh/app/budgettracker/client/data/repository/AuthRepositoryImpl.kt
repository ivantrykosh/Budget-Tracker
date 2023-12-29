package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TokenDto
import com.ivantrykosh.app.budgettracker.client.data.remote.AuthApi
import com.ivantrykosh.app.budgettracker.client.domain.repository.AuthRepository
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import javax.inject.Inject

/**
 * Auth repository implementation
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {
    override suspend fun signUp(request: AuthDto) {
        return api.signUp(request)
    }

    override suspend fun login(request: AuthDto): TokenDto {
        return api.login(request)
    }

    override suspend fun refreshToken(token: String): TokenDto {
        return api.refreshToken(token)
    }

    override suspend fun sendConfirmationEmail(request: AuthDto) {
        return api.sendConfirmationEmail(request)
    }
}