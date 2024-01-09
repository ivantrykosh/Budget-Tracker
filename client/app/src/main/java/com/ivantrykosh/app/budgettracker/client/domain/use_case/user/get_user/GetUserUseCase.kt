package com.ivantrykosh.app.budgettracker.client.domain.use_case.user.get_user

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.toUser
import com.ivantrykosh.app.budgettracker.client.domain.model.User
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Get user use case
 */
class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    /**
     * Invoke get user use case with token
     *
     * @param token user's JWT
     */
    operator fun invoke(token: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getUser("Bearer $token").toUser()
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}