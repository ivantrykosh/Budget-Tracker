package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transaction

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class GetTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(token: String, id: String): Flow<Resource<TransactionDto>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getTransaction(token, id)
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}