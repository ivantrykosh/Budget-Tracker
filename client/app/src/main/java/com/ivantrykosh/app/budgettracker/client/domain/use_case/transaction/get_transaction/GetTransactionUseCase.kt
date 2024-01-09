package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transaction

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Get transaction use case
 */
class GetTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Invoke get transaction use case with token and id
     *
     * @param token user's JWT
     * @param id transaction id to get
     */
    operator fun invoke(token: String, id: String): Flow<Resource<TransactionDto>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getTransaction("Bearer $token", id)
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}