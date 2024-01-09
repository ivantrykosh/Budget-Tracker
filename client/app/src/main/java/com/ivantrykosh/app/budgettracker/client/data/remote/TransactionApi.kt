package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Api for transactions
 */
interface TransactionApi {

    /**
     * Create transaction with token and transactionDto
     *
     * @param token user's JWT
     * @param transactionDto Transaction data
     */
    @POST("create")
    suspend fun createTransaction(@Header("Authorization") token: String, @Body transactionDto: TransactionDto)

    /**
     * Get transaction with token and id
     *
     * @param token user's JWT
     * @param id Transaction ID to get
     */
    @GET("get")
    suspend fun getTransaction(@Header("Authorization") token: String, @Query("id") id: String): TransactionDto

    /**
     * Get transactions with token and accountId
     *
     * @param token user's JWT
     * @param accountId Account ID by which transactions are got
     */
    @GET("get-all-by-account")
    suspend fun getTransactionByAccount(@Header("Authorization") token: String, @Query("id") accountId: String): List<TransactionDto>

    /**
     * Get transactions with token and accountIds
     *
     * @param token user's JWT
     * @param accountIds Account IDs by which transactions are got
     */
    @GET("get-all")
    suspend fun getTransactionByAllAccounts(@Header("Authorization") token: String, @Query("accountIds") accountIds: List<Long>): List<TransactionDto>

    /**
     * Get transactions with token, accountIds, startDate and endDate
     *
     * @param token user's JWT
     * @param accountIds Account IDs by which transactions are got
     * @param startDate start date to get transactions
     * @param endDate end date to get transactions
     */
    @GET("get-all-between-dates")
    suspend fun getTransactionByAllAccountAndDateBetween(@Header("Authorization") token: String, @Query("accountIds") accountIds: List<Long>, @Query("startDate") startDate: String, @Query("endDate") endDate: String): List<TransactionDto>

    /**
     * Update transaction with token and transactionDto
     *
     * @param token user's JWT
     * @param transactionDto Transaction data
     */
    @PUT("update")
    suspend fun updateTransaction(@Header("Authorization") token: String, @Body transactionDto: TransactionDto)

    /**
     * Delete transaction with token and id
     *
     * @param token user's JWT
     * @param id Transaction ID to delete
     */
    @DELETE("delete")
    suspend fun deleteTransaction(@Header("Authorization") token: String, @Query("id") id: String)
}