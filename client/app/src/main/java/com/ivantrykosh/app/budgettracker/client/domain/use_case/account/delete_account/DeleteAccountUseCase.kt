package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.delete_account

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Delete account use case
 */
class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke delete account use case with token and id
     *
     * @param id account id to delete
     */
    operator fun invoke(id: Long): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.deleteAccount(id)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}