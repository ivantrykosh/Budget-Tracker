package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Get all transactions by account IDs use case
 */
class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Invoke get transactions use case with token and accountIds
     *
     * @param token user's JWT
     * @param accountIds accountIds by which transaction are got
     */
    operator fun invoke(token: String, accountIds: List<Long>): Flow<Resource<List<TransactionDto>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getTransactionByAllAccounts("Bearer $token", accountIds)
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}