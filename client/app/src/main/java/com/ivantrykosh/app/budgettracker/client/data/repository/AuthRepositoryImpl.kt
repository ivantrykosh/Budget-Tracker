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

    /**
     * Sign up with request
     *
     * @param request AuthDto request
     */
    override suspend fun signUp(request: AuthDto) {
        return api.signUp(request)
    }

    /**
     * Login with request
     *
     * @param request AuthDto request
     */
    override suspend fun login(request: AuthDto): TokenDto {
        return api.login(request)
    }

    /**
     * Refresh token with token
     *
     * @param token user's JWT
     */
    override suspend fun refreshToken(token: String): TokenDto {
        return api.refreshToken(token)
    }

    /**
     * Send confirmation email with request
     *
     * @param request AuthDto request
     */
    override suspend fun sendConfirmationEmail(request: AuthDto) {
        return api.sendConfirmationEmail(request)
    }
}