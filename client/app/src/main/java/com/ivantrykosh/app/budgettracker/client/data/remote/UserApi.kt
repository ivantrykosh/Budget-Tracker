package com.ivantrykosh.app.budgettracker.client.data.remote

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApi {

    @POST("get")
    suspend fun getUser(): UserDto

    @DELETE("delete")
    suspend fun deleteUser()

    @PATCH("change-password")
    suspend fun changeUserPassword(@Body request: ChangePasswordDto)

    @PATCH("reset-password")
    suspend fun resetUserPassword(@Query("email") email: String)
}