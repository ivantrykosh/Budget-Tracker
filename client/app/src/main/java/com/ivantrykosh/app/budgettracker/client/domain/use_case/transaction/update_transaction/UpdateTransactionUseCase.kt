package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.update_transaction

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Update transaction use case
 */
class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
){
    /**
     * Invoke update transaction use case with token and transactionDto
     *
     * @param token user's JWT
     * @param transactionDto transaction data
     */
    operator fun invoke(token: String, transactionDto: TransactionDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.updateTransaction("Bearer $token", transactionDto)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}