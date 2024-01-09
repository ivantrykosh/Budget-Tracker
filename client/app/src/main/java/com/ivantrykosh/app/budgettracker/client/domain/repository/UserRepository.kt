package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto

/**
 * User repository interface
 */
interface UserRepository {

    /**
     * Get user with token
     *
     * @param token user's JWT
     */
    suspend fun getUser(token: String): UserDto

    /**
     * Delete user with token and request
     *
     * @param token user's JWT
     * @param request AuthDto request
     */
    suspend fun deleteUser(token: String, request: AuthDto)

    /**
     * Change user password with token and request
     *
     * @param token user's JWT
     * @param request ChangePasswordDto request
     */
    suspend fun changeUserPassword(token: String, request: ChangePasswordDto)

    /**
     * Reset user password with email
     *
     * @param email user's email
     */
    suspend fun resetUserPassword(email: String)
}