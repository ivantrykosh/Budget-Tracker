package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.dao.AccountDao
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.model.FullAccount
import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Account repository implementation
 */
class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao
) : AccountRepository {

    /**
     * Create account
     *
     * @param account Account Entity to create
     */
    override suspend fun createAccount(account: AccountEntity) {
        return dao.insertAccount(account)
    }

    /**
     * Get account with id
     *
     * @param id Account ID to get
     */
    override suspend fun getAccount(id: Long): FullAccount {
        return dao.getAccount(id)
    }

    /**
     * Get all accounts
     */
    override suspend fun getAllAccounts(): List<SubAccount> {
        return dao.getAllAccounts()
    }

    /**
     * Update account
     *
     * @param account Account Entity to update
     */
    override suspend fun updateAccount(account: AccountEntity) {
        return dao.updateAccount(account)
    }

    /**
     * Delete account with id
     *
     * @param id Account ID to delete
     */
    override suspend fun deleteAccount(id: Long) {
        return dao.deleteAccount(id)
    }

    /**
     * Delete all accounts
     */
    override suspend fun deleteAllAccounts() {
        return dao.deleteAllAccounts()
    }
}