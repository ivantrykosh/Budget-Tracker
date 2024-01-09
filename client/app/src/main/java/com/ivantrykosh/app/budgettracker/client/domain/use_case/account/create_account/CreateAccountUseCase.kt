package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.create_account

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Create account use case
 */
class CreateAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke create account use case with token and ChangeAccountDto request
     *
     * @param token user's JWT
     * @param request request to create account
     */
    operator fun invoke(token: String, request: ChangeAccountDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.createAccount("Bearer $token", request)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}