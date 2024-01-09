package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TokenDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto

/**
 * Auth repository interface
 */
interface AuthRepository {

    /**
     * Sign up with request
     *
     * @param request AuthDto request
     */
    suspend fun signUp(request: AuthDto)

    /**
     * Login with request
     *
     * @param request AuthDto request
     */
    suspend fun login(request: AuthDto): TokenDto

    /**
     * Refresh token with token
     *
     * @param token user's JWT
     */
    suspend fun refreshToken(token: String): TokenDto

    /**
     * Send confirmation email with request
     *
     * @param request AuthDto request
     */
    suspend fun sendConfirmationEmail(request: AuthDto)
}