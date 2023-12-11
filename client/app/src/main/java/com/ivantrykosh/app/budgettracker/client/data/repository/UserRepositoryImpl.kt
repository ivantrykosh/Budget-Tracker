package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.UserApi
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {
    override suspend fun getUser(): UserDto {
        return api.getUser()
    }

    override suspend fun deleteUser() {
        return api.deleteUser()
    }

    override suspend fun changeUserPassword(request: ChangePasswordDto) {
        return api.changeUserPassword(request)
    }

    override suspend fun resetUserPassword(email: String) {
        return api.resetUserPassword(email)
    }
}