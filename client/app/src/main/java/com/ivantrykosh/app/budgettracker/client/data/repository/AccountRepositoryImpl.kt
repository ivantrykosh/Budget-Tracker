package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.AccountApi
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountWithAccountUsersDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Account repository implementation
 */
class AccountRepositoryImpl @Inject constructor(
    private val api: AccountApi
) : AccountRepository {
    override suspend fun createAccount(token: String, request: ChangeAccountDto) {
        return api.createAccount(token, request)
    }

    override suspend fun getAccount(token: String, id: String): AccountWithAccountUsersDto {
        return api.getAccount(token, id)
    }

    override suspend fun getAllAccounts(token: String): List<AccountDto> {
        return api.getAllAccounts(token)
    }

    override suspend fun updateAccount(token: String, id: String, request: ChangeAccountDto) {
        return api.updateAccount(token, id, request)
    }

    override suspend fun deleteAccount(token: String, id: String) {
        return api.deleteAccount(token, id)
    }

    override suspend fun deleteAllAccounts(token: String) {
        return api.deleteAllAccounts(token)
    }
}