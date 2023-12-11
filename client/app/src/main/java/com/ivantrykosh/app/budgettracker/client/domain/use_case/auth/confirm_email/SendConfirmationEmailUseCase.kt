package com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.confirm_email

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.repository.AuthRepository
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SendConfirmationEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(request: AuthDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.sendConfirmationEmail(request)
            emit(Resource.Success("Success"))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}