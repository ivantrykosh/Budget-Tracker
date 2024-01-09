package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AccountWithAccountUsersDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api for accounts
 */
interface AccountApi {

    /**
     * Create account with token and request
     *
     * @param token user's JWT
     * @param request ChangeAccountDto request
     */
    @POST("create")
    suspend fun createAccount(@Header("Authorization") token: String, @Body request: ChangeAccountDto)

    /**
     * Get account with token and id
     *
     * @param token user's JWT
     * @param id Account ID to get
     */
    @GET("get")
    suspend fun getAccount(@Header("Authorization") token: String, @Query("id") id: String): AccountWithAccountUsersDto

    /**
     * Get all accounts with token
     *
     * @param token user's JWT
     */
    @GET("get-all")
    suspend fun getAllAccounts(@Header("Authorization") token: String): List<AccountDto>

    /**
     * Update account with token, id and request
     *
     * @param token user's JWT
     * @param id Account ID to get
     * @param request ChangeAccountDto request
     */
    @PATCH("update")
    suspend fun updateAccount(@Header("Authorization") token: String, @Query("id") id: String, @Body request: ChangeAccountDto)

    /**
     * Delete account with token and id
     *
     * @param token user's JWT
     * @param id Account ID to delete
     */
    @DELETE("delete")
    suspend fun deleteAccount(@Header("Authorization") token: String, @Query("id") id: String)

    /**
     * Delete all accounts with token
     *
     * @param token user's JWT
     */
    @DELETE("delete-all")
    suspend fun deleteAllAccounts(@Header("Authorization") token: String)
}