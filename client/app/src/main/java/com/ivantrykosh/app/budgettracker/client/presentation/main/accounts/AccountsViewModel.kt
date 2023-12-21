package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.create_account.CreateAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.delete_account.DeleteAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_account.GetAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.update_account.UpdateAccountUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.get_user.GetUserUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.user.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

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

    private val _getUserState = mutableStateOf(UserState())
    val getUserState: State<UserState> = _getUserState

    private val _isLoadingGetUser = MutableLiveData<Boolean>(false)
    val isLoadingGetUser: LiveData<Boolean> = _isLoadingGetUser

    private val _getAccountsState = mutableStateOf(AccountsState())
    val getAccountsState: State<AccountsState> = _getAccountsState

    private val _isLoadingGetAccounts = MutableLiveData<Boolean>(false)
    val isLoadingGetAccounts: LiveData<Boolean> = _isLoadingGetAccounts

    private val _getAccountState = mutableStateOf(GetAccountState())
    val getAccountState: State<GetAccountState> = _getAccountState

    private val _isLoadingGetAccount = MutableLiveData<Boolean>(false)
    val isLoadingGetAccount: LiveData<Boolean> = _isLoadingGetAccount

    private val _createAccountState = mutableStateOf(CreateAccountState())
    val createAccountState: State<CreateAccountState> = _createAccountState

    private val _isLoadingCreateAccount = MutableLiveData<Boolean>(false)
    val isLoadingCreateAccount: LiveData<Boolean> = _isLoadingCreateAccount

    private val _updateAccountState = mutableStateOf(UpdateAccountState())
    val updateAccountState: State<UpdateAccountState> = _updateAccountState

    private val _isLoadingUpdateAccount = MutableLiveData<Boolean>(false)
    val isLoadingUpdateAccount: LiveData<Boolean> = _isLoadingUpdateAccount

    private val _deleteAccountState = mutableStateOf(DeleteAccountState())
    val deleteAccountState: State<DeleteAccountState> = _deleteAccountState

    private val _isLoadingDeleteAccount = MutableLiveData<Boolean>(false)
    val isLoadingDeleteAccount: LiveData<Boolean> = _isLoadingDeleteAccount

    fun checkName(name: String): Boolean {
        return name.isNotBlank() && name.length < 25
    }

    fun checkEmail(email: String?): Boolean {
        return email.isNullOrBlank() || (email.length <= 320 && email.matches(Regex(EMAIL_REGEX)))
    }

    fun getUser() {
        AppPreferences.jwtToken?.let { token ->
            getUser(token)
        } ?: run {
            _getUserState.value = UserState(error = "No JWT token found")
        }
    }

    fun getAccounts() {
        AppPreferences.jwtToken?.let { token ->
            getAccounts(token)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    fun getAccount(id: String) {
        AppPreferences.jwtToken?.let { token ->
            getAccount(token, id)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    fun createAccount(changeAccountDto: ChangeAccountDto) {
        AppPreferences.jwtToken?.let { token ->
            createAccount(token, changeAccountDto)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    fun updateAccount(id: String, changeAccountDto: ChangeAccountDto) {
        AppPreferences.jwtToken?.let { token ->
            updateAccount(token, id, changeAccountDto)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    fun deleteAccount(id: String) {
        AppPreferences.jwtToken?.let { token ->
            deleteAccount(token, id)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    private fun getUser(token: String) {
        _isLoadingGetUser.value = true
        getUserUseCase(token).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getUserState.value = UserState(user = result.data)
                    _isLoadingGetUser.value = false
                }
                is Resource.Error -> {
                    _getUserState.value = UserState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingGetUser.value = false
                }
                is Resource.Loading -> {
                    _getUserState.value = UserState(isLoading = true)
                    _isLoadingGetUser.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getAccounts(token: String) {
        _isLoadingGetAccounts.value = true
        getAccountsUseCase(token).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getAccountsState.value = AccountsState(accounts = result.data ?: emptyList())
                    _isLoadingGetAccounts.value = false
                }
                is Resource.Error -> {
                    _getAccountsState.value = AccountsState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingGetAccounts.value = false
                }
                is Resource.Loading -> {
                    _getAccountsState.value = AccountsState(isLoading = true)
                    _isLoadingGetAccounts.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getAccount(token: String, id: String) {
        _isLoadingGetAccount.value = true
        getAccountUseCase(token, id).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getAccountState.value = GetAccountState(account = result.data)
                    _isLoadingGetAccount.value = false
                }
                is Resource.Error -> {
                    _getAccountState.value = GetAccountState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingGetAccount.value = false
                }
                is Resource.Loading -> {
                    _getAccountState.value = GetAccountState(isLoading = true)
                    _isLoadingGetAccount.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun createAccount(token: String, changeAccountDto: ChangeAccountDto) {
        _isLoadingCreateAccount.value = true
        if (!checkName(changeAccountDto.name)) {
            _createAccountState.value = CreateAccountState(error = "Invalid name")
            _isLoadingCreateAccount.value = false
        } else if (!checkEmail(changeAccountDto.email2)) {
            _createAccountState.value = CreateAccountState(error = "Invalid email2")
            _isLoadingCreateAccount.value = false
        } else if (!checkEmail(changeAccountDto.email3)) {
            _createAccountState.value = CreateAccountState(error = "Invalid email3")
            _isLoadingCreateAccount.value = false
        } else if (!checkEmail(changeAccountDto.email4)) {
            _createAccountState.value = CreateAccountState(error = "Invalid email4")
            _isLoadingCreateAccount.value = false
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
                        _isLoadingCreateAccount.value = false
                    }

                    is Resource.Error -> {
                        _createAccountState.value = CreateAccountState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                        _isLoadingCreateAccount.value = false
                    }

                    is Resource.Loading -> {
                        _createAccountState.value = CreateAccountState(isLoading = true)
                        _isLoadingCreateAccount.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun updateAccount(token: String, id: String, changeAccountDto: ChangeAccountDto) {
        _isLoadingUpdateAccount.value = true
        if (id.isBlank()) {
            _updateAccountState.value = UpdateAccountState(error = "Invalid id")
            _isLoadingUpdateAccount.value = false
        } else if (!checkName(changeAccountDto.name)) {
            _updateAccountState.value = UpdateAccountState(error = "Invalid name")
            _isLoadingUpdateAccount.value = false
        } else if (!checkEmail(changeAccountDto.email2)) {
            _updateAccountState.value = UpdateAccountState(error = "Invalid email2")
            _isLoadingUpdateAccount.value = false
        } else if (!checkEmail(changeAccountDto.email3)) {
            _updateAccountState.value = UpdateAccountState(error = "Invalid email3")
            _isLoadingUpdateAccount.value = false
        } else if (!checkEmail(changeAccountDto.email4)) {
            _updateAccountState.value = UpdateAccountState(error = "Invalid email4")
            _isLoadingUpdateAccount.value = false
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
                        _isLoadingUpdateAccount.value = false
                    }

                    is Resource.Error -> {
                        _updateAccountState.value = UpdateAccountState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                        _isLoadingUpdateAccount.value = false
                    }

                    is Resource.Loading -> {
                        _updateAccountState.value = UpdateAccountState(isLoading = true)
                        _isLoadingUpdateAccount.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun deleteAccount(token: String, id: String) {
        _isLoadingDeleteAccount.value = true
        deleteAccountUseCase(token, id).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _deleteAccountState.value = DeleteAccountState()
                    _isLoadingDeleteAccount.value = false
                }
                is Resource.Error -> {
                    _deleteAccountState.value = DeleteAccountState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingDeleteAccount.value = false
                }
                is Resource.Loading -> {
                    _deleteAccountState.value = DeleteAccountState(isLoading = true)
                    _isLoadingDeleteAccount.value = true
                }
            }
        }.launchIn(viewModelScope)
    }
}