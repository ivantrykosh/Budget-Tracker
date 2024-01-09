package com.ivantrykosh.app.budgettracker.client.domain.use_case.user.change_password

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Change password use case
 */
class ChangePasswordUseCase @Inject constructor(
    private val repository: UserRepository
) {
    /**
     * Invoke change password use case with token and request
     *
     * @param token user's JWT
     * @param request ChangePasswordDto request
     */
    operator fun invoke(token: String, request: ChangePasswordDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.changeUserPassword("Bearer $token", request)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}