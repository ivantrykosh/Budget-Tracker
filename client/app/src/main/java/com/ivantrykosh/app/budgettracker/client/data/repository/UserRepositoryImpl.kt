package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.UserApi
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import javax.inject.Inject

/**
 * User repository implementation
 */
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {

    /**
     * Get user with token
     *
     * @param token user's JWT
     */
    override suspend fun getUser(token: String): UserDto {
        return api.getUser(token)
    }

    /**
     * Delete user with token and request
     *
     * @param token user's JWT
     * @param request AuthDto request
     */
    override suspend fun deleteUser(token: String, request: AuthDto) {
        return api.deleteUser(token, request)
    }

    /**
     * Change user password with token and request
     *
     * @param token user's JWT
     * @param request ChangePasswordDto request
     */
    override suspend fun changeUserPassword(token: String, request: ChangePasswordDto) {
        return api.changeUserPassword(token, request)
    }

    /**
     * Reset user password with email
     *
     * @param email user's email
     */
    override suspend fun resetUserPassword(email: String) {
        return api.resetUserPassword(email)
    }
}