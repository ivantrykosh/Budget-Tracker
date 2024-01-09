package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto
import okhttp3.internal.http.hasBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query

/**
 * Api for user
 */
interface UserApi {

    /**
     * Get user with token
     *
     * @param token user's JWT
     */
    @GET("get")
    suspend fun getUser(@Header("Authorization") token: String): UserDto

    /**
     * Delete user with token and request
     *
     * @param token user's JWT
     * @param request AuthDto request
     */
    @HTTP(method = "DELETE", path = "delete", hasBody = true)
    suspend fun deleteUser(@Header("Authorization") token: String, @Body request: AuthDto)

    /**
     * Change user password with token and request
     *
     * @param token user's JWT
     * @param request ChangePasswordDto request
     */
    @PATCH("change-password")
    suspend fun changeUserPassword(@Header("Authorization") token: String, @Body request: ChangePasswordDto)

    /**
     * Reset user password with email
     *
     * @param email user's email
     */
    @PATCH("reset-password")
    suspend fun resetUserPassword(@Query("email") email: String)
}