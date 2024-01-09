package com.ivantrykosh.app.budgettracker.client.common

/**
 * App constants
 */
object Constants {
    /**
     * Name of app preferences
     */
    const val PREFERENCES = "Budgetracker"

    /**
     * Object with API URLs
     */
    object URL {
        const val BASE_URL = "http://192.168.1.11:8080/"
        const val AUTH_URL = "api/v1/auth/"
        const val USER_URL = "api/v1/users/"
        const val ACCOUNT_URL = "api/v1/accounts/"
        const val TRANSACTION_URL = "api/v1/transactions/"
    }

    /**
     * Available currencies and theirs symbols
     */
    val CURRENCIES = mapOf("USD" to "$", "UAH" to "₴", "EUR" to "€")

    /**
     * Available date formats
     */
    val DATE_FORMATS = setOf("dd/MM/yyyy", "dd.MM.yyyy", "MM/dd/yyyy", "MM.dd.yyyy", "yyyy-MM-dd")

    /**
     * Notification data
     */
    object Notification {
        const val CHANNEL_NAME = "Budgetracker"
        const val CHANNEL_DESCRIPTION = "Notifications"
        const val CHANNEL_ID = "daily_notifications_id"
    }

    /**
     * Error status codes
     */
    object ErrorStatusCodes {
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val CONFLICT = 409
        const val NETWORK_ERROR = 601
        const val TOKEN_NOT_FOUND = 602
        const val INVALID_REQUEST = 603
        const val CLIENT_ERROR = 604
    }
}