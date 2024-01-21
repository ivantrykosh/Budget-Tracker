package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.model.FullAccount
import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount

/**
 * Account repository interface
 */
interface AccountRepository {

    /**
     * Create account
     *
     * @param account AccountEntity to create
     */
    suspend fun createAccount(account: AccountEntity)

    /**
     * Get full account with id
     *
     * @param id Account ID to get
     */
    suspend fun getAccount(id: Long): FullAccount

    /**
     * Get all accounts
     */
    suspend fun getAllAccounts(): List<SubAccount>

    /**
     * Update account
     *
     * @param account AccountEntity to update
     */
    suspend fun updateAccount(account: AccountEntity)

    /**
     * Delete account with id
     *
     * @param id Account ID to delete
     */
    suspend fun deleteAccount(id: Long)

    /**
     * Delete all accounts
     */
    suspend fun deleteAllAccounts()
}