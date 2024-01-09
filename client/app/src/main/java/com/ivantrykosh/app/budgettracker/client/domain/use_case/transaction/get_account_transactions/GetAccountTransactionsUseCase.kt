package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_account_transactions

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Get account's transactions use case
 */
class GetAccountTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Invoke get account transactions use case with token and accountId
     *
     * @param token user's JWT
     * @param accountId accountId by which transactions are got
     */
    operator fun invoke(token: String, accountId: String): Flow<Resource<List<TransactionDto>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getTransactionByAccount("Bearer $token", accountId)
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}