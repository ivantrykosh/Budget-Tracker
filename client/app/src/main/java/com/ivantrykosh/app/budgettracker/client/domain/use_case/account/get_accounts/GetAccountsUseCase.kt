package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Get all user accounts use case
 */
class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke get accounts use case
     */
    operator fun invoke(): Flow<Resource<List<SubAccount>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getAllAccounts()
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