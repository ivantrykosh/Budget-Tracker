package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.delete_all_accounts

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Delete all accounts use case
 */
class DeleteAllAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke delete all accounts use case with token
     *
     * @param token user's JWT
     */
    operator fun invoke(token: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.deleteAllAccounts("Bearer $token")
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}