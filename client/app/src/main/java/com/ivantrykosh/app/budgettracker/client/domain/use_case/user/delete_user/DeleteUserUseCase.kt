package com.ivantrykosh.app.budgettracker.client.domain.use_case.user.delete_user

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Delete user use case
 */
class DeleteUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    /**
     * Invoke delete user use case with token and request
     *
     * @param token user's JWT
     * @param request AuthDto request
     */
    operator fun invoke(token: String, request: AuthDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.deleteUser("Bearer $token", request)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            println(e.message)
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}