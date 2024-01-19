package com.ivantrykosh.app.budgettracker.client.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.model.FullAccount
import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE accountId = :accountId")
    suspend fun deleteAccount(accountId: Long)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()

    @Query("SELECT a.accountId, a.name, SUM(CASE WHEN t.value > 0 THEN t.value ELSE 0 END) as incomesSum, SUM(CASE WHEN t.value < 0 THEN t.value ELSE 0 END) as expensesSum, SUM(t.value) as total FROM accounts a LEFT OUTER JOIN transactions t ON a.accountId = t.accountId WHERE a.accountId = :accountId GROUP BY a.accountId, a.name")
    suspend fun getAccount(accountId: Long): FullAccount

    @Query("SELECT a.accountId, a.name, SUM(t.value) as total FROM accounts a LEFT OUTER JOIN transactions t ON t.accountId = a.accountId WHERE a.accountId IS NOT NULL GROUP BY a.accountId, a.name")
    suspend fun getAccounts(): List<SubAccount>
}