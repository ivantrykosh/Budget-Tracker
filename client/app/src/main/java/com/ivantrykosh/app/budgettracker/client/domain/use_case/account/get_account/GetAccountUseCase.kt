package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_account

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.FullAccount
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Get account use case
 */
class GetAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Invoke get account use case
     *
     * @param id account id to get
     */
    operator fun invoke(id: Long): Flow<Resource<FullAccount>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getAccount(id)
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