package com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.login

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.repository.AuthRepository
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Login use case
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(request: AuthDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.login(request)
            emit(Resource.Success(result.token))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}