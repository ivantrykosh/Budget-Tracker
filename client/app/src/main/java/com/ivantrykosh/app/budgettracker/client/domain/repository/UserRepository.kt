package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto

interface UserRepository {
    suspend fun getUser(token: String): UserDto

    suspend fun deleteUser(token: String)

    suspend fun changeUserPassword(token: String, request: ChangePasswordDto)

    suspend fun resetUserPassword(email: String)
}