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

    /**
     * Create account with token and request
     *
     * @param token user's JWT
     * @param request ChangeAccountDto request
     */
    override suspend fun createAccount(token: String, request: ChangeAccountDto) {
        return api.createAccount(token, request)
    }

    /**
     * Get account with token and id
     *
     * @param token user's JWT
     * @param id Account ID to get
     */
    override suspend fun getAccount(token: String, id: String): AccountWithAccountUsersDto {
        return api.getAccount(token, id)
    }

    /**
     * Get all accounts with token
     *
     * @param token user's JWT
     */
    override suspend fun getAllAccounts(token: String): List<AccountDto> {
        return api.getAllAccounts(token)
    }

    /**
     * Update account with token, id and request
     *
     * @param token user's JWT
     * @param id Account ID to get
     * @param request ChangeAccountDto request
     */
    override suspend fun updateAccount(token: String, id: String, request: ChangeAccountDto) {
        return api.updateAccount(token, id, request)
    }

    /**
     * Delete account with token and id
     *
     * @param token user's JWT
     * @param id Account ID to delete
     */
    override suspend fun deleteAccount(token: String, id: String) {
        return api.deleteAccount(token, id)
    }

    /**
     * Delete all accounts with token
     *
     * @param token user's JWT
     */
    override suspend fun deleteAllAccounts(token: String) {
        return api.deleteAllAccounts(token)
    }
}