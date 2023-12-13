package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountWithAccountUsersDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto

interface AccountRepository {

    suspend fun createAccount(token: String, request: ChangeAccountDto)

    suspend fun getAccount(token: String, id: String): AccountWithAccountUsersDto

    suspend fun getAllAccounts(token: String): List<AccountWithAccountUsersDto>

    suspend fun updateAccount(token: String, id: String, request: ChangeAccountDto)

    suspend fun deleteAccount(token: String, id: String)
}