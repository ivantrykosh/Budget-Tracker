package com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.refresh_token

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Refresh JWT use case
 */
class RefreshTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(token: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.refreshToken("Bearer $token")
            emit(Resource.Success(result.token))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}