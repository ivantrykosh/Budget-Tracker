package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.util.Date
import javax.inject.Inject

class GetTransactionsBetweenDates @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(token: String, accountIds: List<Long>, startDate: String, endDate: String): Flow<Resource<List<TransactionDto>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getTransactionByAllAccountAndDateBetween("Bearer $token", accountIds, startDate, endDate)
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}