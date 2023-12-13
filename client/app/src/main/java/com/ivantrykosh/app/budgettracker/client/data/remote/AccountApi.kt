package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountWithAccountUsersDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AccountApi {

    @POST("create")
    suspend fun createAccount(@Header("Authorization") token: String, @Body request: ChangeAccountDto)

    @GET("get")
    suspend fun getAccount(@Header("Authorization") token: String, @Query("id") id: String): AccountWithAccountUsersDto

    @GET("get-all")
    suspend fun getAllAccounts(@Header("Authorization") token: String): List<AccountWithAccountUsersDto>

    @PATCH("update")
    suspend fun updateAccount(@Header("Authorization") token: String, @Query("id") id: String, @Body request: ChangeAccountDto)

    @DELETE("delete")
    suspend fun deleteAccount(@Header("Authorization") token: String, @Query("id") id: String)
}