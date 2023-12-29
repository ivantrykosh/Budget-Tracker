package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.create_transaction

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Create transaction use case
 */
class CreateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(token: String, transactionDto: TransactionDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.createTransaction("Bearer $token", transactionDto)
            emit(Resource.Success("Success"))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: Exception) {
            println(e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}