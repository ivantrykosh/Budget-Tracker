package com.ivantrykosh.app.budgettracker.client.domain.use_case.user.reset_password

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(request: AuthDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.resetUserPassword(request.email)
            emit(Resource.Success("Success"))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}