package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.toAccount
import com.ivantrykosh.app.budgettracker.client.domain.model.Account
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(token: String): Flow<Resource<List<Account>>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.getAllAccounts(token).map { it.accountDto.toAccount() }
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}