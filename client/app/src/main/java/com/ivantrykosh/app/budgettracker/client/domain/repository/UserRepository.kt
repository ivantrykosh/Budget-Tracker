package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto

interface UserRepository {
    suspend fun getUser(): UserDto

    suspend fun deleteUser()

    suspend fun changeUserPassword(request: ChangePasswordDto)

    suspend fun resetUserPassword(email: String)
}