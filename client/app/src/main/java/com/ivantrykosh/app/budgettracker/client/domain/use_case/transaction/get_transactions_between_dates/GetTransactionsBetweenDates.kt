package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject

/**
 * Get all transactions between dates and account IDs use case
 */
class GetTransactionsBetweenDates @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Invoke get transactions between dates use case with accountIds, startDate and endDate
     *
     * @param accountIds account IDs by which transaction are get
     * @param startDate start date to get transactions
     * @param endDate end date to get transactions
     */
    operator fun invoke(accountIds: List<Long>, startDate: Date, endDate: Date): Flow<Resource<List<SubTransaction>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getTransactionByAllAccountAndDateBetween(accountIds, startDate, endDate)
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: IOException) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.CLIENT_ERROR))
        }
    }
}