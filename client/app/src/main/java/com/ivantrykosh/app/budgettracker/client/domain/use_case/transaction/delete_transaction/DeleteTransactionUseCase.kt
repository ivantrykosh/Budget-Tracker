package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.delete_transaction

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Delete transaction use case
 */
class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Invoke delete transaction use case with token and id
     *
     * @param token user's JWT
     * @param id transaction id to delete
     */
    operator fun invoke(token: String, id: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.deleteTransaction("Bearer $token", id)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}