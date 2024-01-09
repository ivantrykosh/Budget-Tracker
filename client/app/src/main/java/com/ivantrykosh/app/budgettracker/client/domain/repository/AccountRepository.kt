package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountWithAccountUsersDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto

/**
 * Account repository interface
 */
interface AccountRepository {

    /**
     * Create account with token and request
     *
     * @param token user's JWT
     * @param request ChangeAccountDto request
     */
    suspend fun createAccount(token: String, request: ChangeAccountDto)

    /**
     * Get account with token and id
     *
     * @param token user's JWT
     * @param id Account ID to get
     */
    suspend fun getAccount(token: String, id: String): AccountWithAccountUsersDto

    /**
     * Get all accounts with token
     *
     * @param token user's JWT
     */
    suspend fun getAllAccounts(token: String): List<AccountDto>

    /**
     * Update account with token, id and request
     *
     * @param token user's JWT
     * @param id Account ID to get
     * @param request ChangeAccountDto request
     */
    suspend fun updateAccount(token: String, id: String, request: ChangeAccountDto)

    /**
     * Delete account with token and id
     *
     * @param token user's JWT
     * @param id Account ID to delete
     */
    suspend fun deleteAccount(token: String, id: String)

    /**
     * Delete all accounts with token
     *
     * @param token user's JWT
     */
    suspend fun deleteAllAccounts(token: String)
}