package com.ivantrykosh.app.budgettracker.client.data.remote.dto

/**
 * Data class for auth request
 */
data class AuthDto(
    val email: String, // User email
    val passwordHash: String // User password
)
