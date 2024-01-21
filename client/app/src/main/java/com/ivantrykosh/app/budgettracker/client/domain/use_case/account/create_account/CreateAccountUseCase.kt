package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.create_account

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Create account use case
 */
class CreateAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke create account use case
     *
     * @param account account to create
     */
    operator fun invoke(account: AccountEntity): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.createAccount(account)
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