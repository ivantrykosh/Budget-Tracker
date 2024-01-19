package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.model.FullAccount
import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount

/**
 * Account repository interface
 */
interface AccountRepository {

    /**
     * Create account with token and request
     *
     * @param account ChangeAccountDto request
     */
    suspend fun createAccount(account: AccountEntity)

    /**
     * Get account with token and id
     *
     * @param id Account ID to get
     */
    suspend fun getAccount(id: Long): FullAccount

    /**
     * Get all accounts with token
     */
    suspend fun getAllAccounts(): List<SubAccount>

    /**
     * Update account with token, id and request
     *
     * @param account ChangeAccountDto request
     */
    suspend fun updateAccount(account: AccountEntity)

    /**
     * Delete account with token and id
     *
     * @param id Account ID to delete
     */
    suspend fun deleteAccount(id: Long)

    /**
     * Delete all accounts with token
     *
     */
    suspend fun deleteAllAccounts()
}