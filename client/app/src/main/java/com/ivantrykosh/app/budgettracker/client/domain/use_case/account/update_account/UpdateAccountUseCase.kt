package com.ivantrykosh.app.budgettracker.client.domain.use_case.account.update_account

import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class UpdateAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(token: String, id: String, changeAccountDto: ChangeAccountDto): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val result = repository.updateAccount("Bearer $token", id, changeAccountDto)
            emit(Resource.Success("Success"))
        } catch (e: HttpException) {
            emit(Resource.Error("${e.code()} ${e.localizedMessage ?: "An unexpected error occurred"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        }
    }
}