package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_account

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.toAccountDetails
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountDetails
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Get account use case
 */
class GetAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke get account use case with token and id
     *
     * @param token user's JWT
     * @param id account id to get
     */
    operator fun invoke(token: String, id: String): Flow<Resource<AccountDetails>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getAccount("Bearer $token", id).toAccountDetails()
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}