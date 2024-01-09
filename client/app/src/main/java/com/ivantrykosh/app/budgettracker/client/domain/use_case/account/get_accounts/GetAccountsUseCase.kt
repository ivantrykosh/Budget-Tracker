package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.toAccount
import com.ivantrykosh.app.budgettracker.client.domain.model.Account
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Get all user accounts use case
 */
class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke get accounts use case with token
     *
     * @param token user's JWT
     */
    operator fun invoke(token: String): Flow<Resource<List<Account>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getAllAccounts("Bearer $token").map { it.toAccount() }
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}