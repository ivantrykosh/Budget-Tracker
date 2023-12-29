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

    @GET("get")
    suspend fun getUser(@Header("Authorization") token: String): UserDto

    @HTTP(method = "DELETE", path = "delete", hasBody = true)
    suspend fun deleteUser(@Header("Authorization") token: String, @Body request: AuthDto)

    @PATCH("change-password")
    suspend fun changeUserPassword(@Header("Authorization") token: String, @Body request: ChangePasswordDto)

    @PATCH("reset-password")
    suspend fun resetUserPassword(@Query("email") email: String)
}