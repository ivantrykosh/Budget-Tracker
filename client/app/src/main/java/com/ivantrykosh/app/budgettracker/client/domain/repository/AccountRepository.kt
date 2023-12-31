package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountWithAccountUsersDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto

/**
 * Account repository interface
 */
interface AccountRepository {

    suspend fun createAccount(token: String, request: ChangeAccountDto)

    suspend fun getAccount(token: String, id: String): AccountWithAccountUsersDto

    suspend fun getAllAccounts(token: String): List<AccountDto>

    suspend fun updateAccount(token: String, id: String, request: ChangeAccountDto)

    suspend fun deleteAccount(token: String, id: String)

    suspend fun deleteAllAccounts(token: String)
}