package com.ivantrykosh.app.budgettracker.client.common

/**
 * App constants
 */
object Constants {
    const val PREFERENCES = "Budgetracker"

    const val BASE_URL = "http://192.168.1.7:8080/"
    const val AUTH_URL = "api/v1/auth/"
    const val USER_URL = "api/v1/users/"
    const val ACCOUNT_URL = "api/v1/accounts/"
    const val TRANSACTION_URL = "api/v1/transactions/"

    val CURRENCIES = mapOf("USD" to "$", "UAH" to "₴", "EUR" to "€")
    val DATE_FORMATS = setOf("dd/MM/yyyy", "dd.MM.yyyy", "MM/dd/yyyy", "MM.dd.yyyy", "yyyy-MM-dd")

    object Notification {
        const val CHANNEL_NAME = "Budgetracker"
        const val CHANNEL_DESCRIPTION = "Notifications"
        const val CHANNEL_ID = "daily_notifications_id"
    }
}