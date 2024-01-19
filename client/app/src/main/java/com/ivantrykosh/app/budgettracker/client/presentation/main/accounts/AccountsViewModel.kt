package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.create_account.CreateAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.delete_account.DeleteAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_account.GetAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.update_account.UpdateAccountUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.CreateAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.DeleteAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.UpdateAccountState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Accounts view model
 */
@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountUseCase: GetAccountUseCase,
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {

    private val _getAccountsState = MutableLiveData(GetAccountsState())
    val getAccountsState: LiveData<GetAccountsState> = _getAccountsState

    private val _getAccountState = MutableLiveData(GetAccountState())
    val getAccountState: LiveData<GetAccountState> = _getAccountState

    private val _createAccountState = MutableLiveData(CreateAccountState())
    val createAccountState: LiveData<CreateAccountState> = _createAccountState

    private val _updateAccountState = MutableLiveData(UpdateAccountState())
    val updateAccountState: LiveData<UpdateAccountState> = _updateAccountState

    private val _deleteAccountState = MutableLiveData(DeleteAccountState())
    val deleteAccountState: LiveData<DeleteAccountState> = _deleteAccountState

    /**
     * Check account name
     */
    fun checkName(name: String, id: Long = 0): Boolean {
        return name.isNotBlank() && name.length < 25 && _getAccountsState.value!!.accounts.none { it.name == name && it.accountId != id }
    }

    /**
     * Get user accounts by JWT
     *
     */
    fun getAccounts() {
        _getAccountsState.value = GetAccountsState(isLoading = true)
        getAccountsUseCase().onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getAccountsState.value = GetAccountsState(accounts = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _getAccountsState.value = GetAccountsState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _getAccountsState.value = GetAccountsState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Get account by ID
     *
     * @param id account ID to get
     */
    fun getAccount(id: Long) {
        _getAccountState.value = GetAccountState(isLoading = true)
        getAccountUseCase(id).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getAccountState.value = GetAccountState(account = result.data)
                }
                is Resource.Error -> {
                    _getAccountState.value = GetAccountState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _getAccountState.value = GetAccountState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Create account with ChangeAccountDto
     *
     * @param name ChangeAccountDto instance to create account
     */
    fun createAccount(name: String) {
        _createAccountState.value = CreateAccountState(isLoading = true)
        if (!checkName(name)) {
            _createAccountState.value = CreateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val newAccount = AccountEntity(name = name)
            createAccountUseCase(newAccount).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _createAccountState.value = CreateAccountState()
                    }
                    is Resource.Error -> {
                        _createAccountState.value = CreateAccountState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }
                    is Resource.Loading -> {
                        _createAccountState.value = CreateAccountState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Update account with ID and ChangeAccountDto
     *
     * @param id account ID to update
     * @param name ChangeAccountDto instance to create account
     */
    fun updateAccount(id: Long, name: String) {
        _updateAccountState.value = UpdateAccountState(isLoading = true)
        if (!checkName(name, id)) {
            _updateAccountState.value = UpdateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val newAccount = AccountEntity(id, name)
            updateAccountUseCase(newAccount).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _updateAccountState.value = UpdateAccountState()
                    }
                    is Resource.Error -> {
                        _updateAccountState.value = UpdateAccountState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }
                    is Resource.Loading -> {
                        _updateAccountState.value = UpdateAccountState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Delete account by ID
     *
     * @param id account ID to delete
     */
    fun deleteAccount(id: Long) {
        _deleteAccountState.value = DeleteAccountState(isLoading = true)
        deleteAccountUseCase(id).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _deleteAccountState.value = DeleteAccountState()
                }
                is Resource.Error -> {
                    _deleteAccountState.value = DeleteAccountState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _deleteAccountState.value = DeleteAccountState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}