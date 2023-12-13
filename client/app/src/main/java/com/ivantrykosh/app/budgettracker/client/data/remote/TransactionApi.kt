package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import java.util.Date

interface TransactionApi {

    @POST("create")
    suspend fun createTransaction(@Header("Authorization") token: String, transactionDto: TransactionDto)

    @GET("get")
    suspend fun getTransaction(@Header("Authorization") token: String, @Query("id") id: String): TransactionDto

    @GET("get-all-by-account")
    suspend fun getTransactionByAccount(@Header("Authorization") token: String, @Query("id") accountId: String): List<TransactionDto>

    @GET("get-all")
    suspend fun getTransactionByAllAccounts(@Header("Authorization") token: String, @Query("id") accountIds: List<Long>): List<TransactionDto>

    @GET("get-all-between-dates")
    suspend fun getTransactionByAllAccountAndDateBetween(@Header("Authorization") token: String, @Query("id") accountIds: List<Long>, @Query("startDate") startDate: Date, @Query("endDate") endDate: Date): List<TransactionDto>

    @PUT("update")
    suspend fun updateTransaction(@Header("Authorization") token: String, transactionDto: TransactionDto)

    @DELETE("delete")
    suspend fun deleteTransaction(@Header("Authorization") token: String, @Query("id") id: String)
}