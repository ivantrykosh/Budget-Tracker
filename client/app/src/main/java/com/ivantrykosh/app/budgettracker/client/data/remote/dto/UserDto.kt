package com.ivantrykosh.app.budgettracker.client.data.remote.dto

import com.ivantrykosh.app.budgettracker.client.domain.model.User
import java.util.Date

/**
 * Data class for user
 */
data class UserDto(
    val userId: Long,
    val email: String,
    val registrationDate: Date,
    val isVerified: Boolean,
)

/**
 * Convert from UserDto to User
 */
fun UserDto.toUser(): User {
    return User(
        userId = userId,
        email = email,
        registrationDate = registrationDate,
    )
}