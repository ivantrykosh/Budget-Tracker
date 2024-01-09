package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.update_account

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
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
     * @param token user's JWT
     * @param id account id to update
     * @param changeAccountDto account data
     */
    operator fun invoke(token: String, id: String, changeAccountDto: ChangeAccountDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            repository.updateAccount("Bearer $token", id, changeAccountDto)
            emit(Resource.Success(""))
        } catch (e: HttpException) {
            emit(Resource.Error(e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(Constants.ErrorStatusCodes.NETWORK_ERROR))
        }
    }
}