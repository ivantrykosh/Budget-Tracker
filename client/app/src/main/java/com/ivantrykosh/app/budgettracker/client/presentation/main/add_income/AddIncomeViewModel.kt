package com.ivantrykosh.app.budgettracker.client.presentation.main.add_income

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.create_transaction.CreateTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.AccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.TransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.NoSuchElementException
import javax.inject.Inject

@HiltViewModel
class AddIncomeViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
) : ViewModel() {

    private val _getAccountsState = mutableStateOf(AccountsState())
    val getAccountsState: State<AccountsState> = _getAccountsState

    private val _isLoadingGetAccounts = MutableLiveData<Boolean>(false)
    val isLoadingGetAccounts: LiveData<Boolean> = _isLoadingGetAccounts

    private val _createTransactionState = mutableStateOf(TransactionState())
    val createTransactionState: State<TransactionState> = _createTransactionState

    private val _isLoadingCreateTransaction = MutableLiveData<Boolean>(false)
    val isLoadingCreateTransaction: LiveData<Boolean> = _isLoadingCreateTransaction

    fun checkValue(value: String): Boolean {
        var valid = true
        try {
            if (value.toDouble() == 0.0) {
                valid = false
            }
        } catch (e: NumberFormatException) {
            valid = false
        }
        return valid
    }

    fun parseToCorrectDate(date: String): Date {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return inputFormat.parse(date) ?: Date()
    }

    fun getAccounts() {
        AppPreferences.jwtToken?.let { token ->
            getAccounts(token)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    private fun getAccounts(token: String) {
        _isLoadingGetAccounts.value = true
        getAccountsUseCase(token).onEach { result ->
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

    fun createTransactionDto(accountName: String, category: String, value: Double, date: Date, toFromWhom: String, note: String): TransactionDto? {
        return try {
            val accountId = _getAccountsState.value.accounts.first { it.name == accountName }.accountId
            TransactionDto(
                transactionId = null,
                accountId = accountId,
                category = category,
                value = value,
                date = date,
                toFromWhom = toFromWhom.ifBlank { null },
                note = note.ifBlank { null },
            )
        } catch (e: NoSuchElementException) {
            null
        }
    }

    fun createTransaction(transactionDto: TransactionDto) {
        AppPreferences.jwtToken?.let { token ->
            createTransaction(token, transactionDto)
        } ?: run {
            _createTransactionState.value = TransactionState(error = "No JWT token found")
        }
    }

    private fun createTransaction(token: String, transactionDto: TransactionDto) {
        _isLoadingCreateTransaction.value = true
        createTransactionUseCase(token, transactionDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _createTransactionState.value = TransactionState()
                    _isLoadingCreateTransaction.value = false
                }
                is Resource.Error -> {
                    _createTransactionState.value = TransactionState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingCreateTransaction.value = false
                }
                is Resource.Loading -> {
                    _createTransactionState.value = TransactionState(isLoading = true)
                    _isLoadingCreateTransaction.value = true
                }
            }
        }.launchIn(viewModelScope)
    }
}