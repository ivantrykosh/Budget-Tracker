package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.create_account.CreateAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.delete_account.DeleteAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_account.GetAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.update_account.UpdateAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.get_user.GetUserUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.CreateAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.DeleteAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.UpdateAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state.GetUserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Accounts view model
 */
@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountUseCase: GetAccountUseCase,
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {

    companion object {
        private const val EMAIL_REGEX =
            "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
    }

    private val _getUserState = MutableLiveData(GetUserState())
    val getUserState: LiveData<GetUserState> = _getUserState

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
    fun checkName(name: String): Boolean {
        return name.isNotBlank() && name.length < 25
    }

    /**
     * Check email
     */
    fun checkEmail(email: String?): Boolean {
        return email.isNullOrBlank() || (email.length <= 320 && email.matches(Regex(EMAIL_REGEX)))
    }

    /**
     * Get user
     */
    fun getUser() {
        AppPreferences.jwtToken?.let { token ->
            getUser(token)
        } ?: run {
            _getUserState.value = GetUserState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Get user accounts
     */
    fun getAccounts() {
        AppPreferences.jwtToken?.let { token ->
            getAccounts(token)
        } ?: run {
            _getAccountsState.value = GetAccountsState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Get account by ID
     *
     * @param id account ID to get
     */
    fun getAccount(id: String) {
        AppPreferences.jwtToken?.let { token ->
            getAccount(token, id)
        } ?: run {
            _getAccountState.value = GetAccountState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Create account with ChangeAccountDto
     *
     * @param changeAccountDto ChangeAccountDto instance to create account
     */
    fun createAccount(changeAccountDto: ChangeAccountDto) {
        AppPreferences.jwtToken?.let { token ->
            createAccount(token, changeAccountDto)
        } ?: run {
            _createAccountState.value = CreateAccountState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Update account with ID and ChangeAccountDto
     *
     * @param id account ID to update
     * @param changeAccountDto ChangeAccountDto instance to create account
     */
    fun updateAccount(id: String, changeAccountDto: ChangeAccountDto) {
        AppPreferences.jwtToken?.let { token ->
            updateAccount(token, id, changeAccountDto)
        } ?: run {
            _updateAccountState.value = UpdateAccountState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Delete account by ID
     *
     * @param id account ID to delete
     */
    fun deleteAccount(id: String) {
        AppPreferences.jwtToken?.let { token ->
            deleteAccount(token, id)
        } ?: run {
            _deleteAccountState.value = DeleteAccountState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Get user by JWT
     *
     * @param token user JWT
     */
    private fun getUser(token: String) {
        _getUserState.value = GetUserState(isLoading = true)
        getUserUseCase(token).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getUserState.value = GetUserState(user = result.data)
                }
                is Resource.Error -> {
                    _getUserState.value = GetUserState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _getUserState.value = GetUserState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Get user accounts by JWT
     *
     * @param token user JWT
     */
    private fun getAccounts(token: String) {
        _getAccountsState.value = GetAccountsState(isLoading = true)
        getAccountsUseCase(token).onEach {result ->
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
     * @param token user JWT
     * @param id account ID to get
     */
    private fun getAccount(token: String, id: String) {
        _getAccountState.value = GetAccountState(isLoading = true)
        getAccountUseCase(token, id).onEach {result ->
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
     * @param token user JWT
     * @param changeAccountDto ChangeAccountDto instance to create account
     */
    private fun createAccount(token: String, changeAccountDto: ChangeAccountDto) {
        _createAccountState.value = CreateAccountState(isLoading = true)
        if (!checkName(changeAccountDto.name)) {
            _createAccountState.value = CreateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(changeAccountDto.email2)) {
            _createAccountState.value = CreateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(changeAccountDto.email3)) {
            _createAccountState.value = CreateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(changeAccountDto.email4)) {
            _createAccountState.value = CreateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val newAccountDto = ChangeAccountDto(
                name = changeAccountDto.name,
                email2 = when {
                    changeAccountDto.email2.isNullOrBlank() -> null
                    else -> changeAccountDto.email2
                },
                email3 = when {
                    changeAccountDto.email3.isNullOrBlank() -> null
                    else -> changeAccountDto.email3
                },
                email4 = when {
                    changeAccountDto.email4.isNullOrBlank() -> null
                    else -> changeAccountDto.email4
                }
            )
            createAccountUseCase(token, newAccountDto).onEach { result ->
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
     * @param token user JWT
     * @param id account ID to update
     * @param changeAccountDto ChangeAccountDto instance to create account
     */
    private fun updateAccount(token: String, id: String, changeAccountDto: ChangeAccountDto) {
        _updateAccountState.value = UpdateAccountState(isLoading = true)
        if (id.isBlank()) {
            _updateAccountState.value = UpdateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkName(changeAccountDto.name)) {
            _updateAccountState.value = UpdateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(changeAccountDto.email2)) {
            _updateAccountState.value = UpdateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(changeAccountDto.email3)) {
            _updateAccountState.value = UpdateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(changeAccountDto.email4)) {
            _updateAccountState.value = UpdateAccountState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val newAccountDto = ChangeAccountDto(
                name = changeAccountDto.name,
                email2 = when {
                    changeAccountDto.email2.isNullOrBlank() -> null
                    else -> changeAccountDto.email2
                },
                email3 = when {
                    changeAccountDto.email3.isNullOrBlank() -> null
                    else -> changeAccountDto.email3
                },
                email4 = when {
                    changeAccountDto.email4.isNullOrBlank() -> null
                    else -> changeAccountDto.email4
                }
            )
            updateAccountUseCase(token, id, newAccountDto).onEach { result ->
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
     * @param token user JWT
     * @param id account ID to delete
     */
    private fun deleteAccount(token: String, id: String) {
        _deleteAccountState.value = DeleteAccountState(isLoading = true)
        deleteAccountUseCase(token, id).onEach {result ->
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