package com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.create_transaction

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Create transaction use case
 */
class CreateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Invoke create transaction use case
     *
     * @param transaction transaction data
     */
    operator fun invoke(transaction: Transaction): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.createTransaction(transaction)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: IOException) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.CLIENT_ERROR))
        }
    }
}