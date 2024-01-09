package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.util.Date
import javax.inject.Inject

/**
 * Get all transactions between dates and account IDs use case
 */
class GetTransactionsBetweenDates @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Invoke get transactions between dates use case with token and accountIds, startDate and endDate
     *
     * @param token user's JWT
     * @param accountIds account IDs by which transaction are get
     * @param startDate start date to get transactions
     * @param endDate end date to get transactions
     */
    operator fun invoke(token: String, accountIds: List<Long>, startDate: String, endDate: String): Flow<Resource<List<TransactionDto>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getTransactionByAllAccountAndDateBetween("Bearer $token", accountIds, startDate, endDate)
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}