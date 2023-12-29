package com.ivantrykosh.app.budgettracker.client.data.remote.dto

/**
 * Data class for change password
 */
data class ChangePasswordDto(
    val oldPassword: String,
    val newPassword: String
)
