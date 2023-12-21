package com.ivantrykosh.app.budgettracker.client.common

object Constants {
    const val PREFERENCES = "Budgetracker"

    const val BASE_URL = "http://192.168.1.6:8080/"
    const val AUTH_URL = "api/v1/auth/"
    const val USER_URL = "api/v1/users/"
    const val ACCOUNT_URL = "api/v1/accounts/"
    const val TRANSACTION_URL = "api/v1/transactions/"

    val CURRENCIES = setOf("USD", "UAH")
    val DATE_FORMATS = setOf("dd/MM/yyyy", "dd.MM.yyyy", "MM/dd/yyyy", "MM.dd.yyyy", "yyyy-MM-dd")
}