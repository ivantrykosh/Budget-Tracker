package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.UserApi
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {
    override suspend fun getUser(token: String): UserDto {
        return api.getUser(token)
    }

    override suspend fun deleteUser(token: String) {
        return api.deleteUser(token)
    }

    override suspend fun changeUserPassword(token: String, request: ChangePasswordDto) {
        return api.changeUserPassword(token, request)
    }

    override suspend fun resetUserPassword(email: String) {
        return api.resetUserPassword(email)
    }
}