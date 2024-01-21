package com.ivantrykosh.app.budgettracker.client.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.model.FullAccount
import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount

/**
 * Account data access object
 */
@Dao
interface AccountDao {

    /**
     * Insert account with its data to DB.
     *
     * @param account Account Entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity)

    /**
     * Update account in DB.
     *
     * @param account Account Entity to update
     */
    @Update
    suspend fun updateAccount(account: AccountEntity)

    /**
     * Delete account and its transactions from DB.
     *
     * @param accountId ID of account to delete
     */
    @Query("DELETE FROM accounts WHERE accountId = :accountId")
    suspend fun deleteAccount(accountId: Long)

    /**
     * Delete all accounts and their transactions from DB.
     */
    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()

    /**
     * Get account from DB. Also calculate total, income and expense sum of account's transactions
     *
     * @param accountId ID of account to get
     */
    @Query("SELECT a.accountId, a.name, SUM(CASE WHEN t.value > 0 THEN t.value ELSE 0 END) as incomesSum, SUM(CASE WHEN t.value < 0 THEN t.value ELSE 0 END) as expensesSum, SUM(t.value) as total FROM accounts a LEFT OUTER JOIN transactions t ON a.accountId = t.accountId WHERE a.accountId = :accountId GROUP BY a.accountId, a.name")
    suspend fun getAccount(accountId: Long): FullAccount

    /**
     * Get all accounts from DB. Also calculate total sum of accounts' transactions
     */
    @Query("SELECT a.accountId, a.name, SUM(t.value) as total FROM accounts a LEFT OUTER JOIN transactions t ON t.accountId = a.accountId WHERE a.accountId IS NOT NULL GROUP BY a.accountId, a.name")
    suspend fun getAllAccounts(): List<SubAccount>
}