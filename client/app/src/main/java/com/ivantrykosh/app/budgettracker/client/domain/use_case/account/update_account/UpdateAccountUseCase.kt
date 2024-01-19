package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.update_account

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Update account use case
 */
class UpdateAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke update account use case with token, id and changeAccountDto
     *
     * @param account account data
     */
    operator fun invoke(account: AccountEntity): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.updateAccount(account)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}