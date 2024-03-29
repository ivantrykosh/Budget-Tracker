package com.ivantrykosh.app.budgettracker.client.presentation.main.add_transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.create_transaction.CreateTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.CreateTransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.NoSuchElementException
import javax.inject.Inject

/**
 * Add transaction view model
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
) : ViewModel() {

    private val _getAccountsState = MutableLiveData(GetAccountsState())
    val getAccountsState: LiveData<GetAccountsState> = _getAccountsState

    private val _createTransactionState = MutableLiveData(CreateTransactionState())
    val createTransactionState: LiveData<CreateTransactionState> = _createTransactionState

    /**
     * Check value of transaction
     *
     * @param value transaction value
     */
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

    /**
     * Parse string to date
     */
    private fun parseStringToDate(date: String): Date {
        val inputFormat = SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault())
        return inputFormat.parse(date) ?: Date()
    }

    /**
     * Parse date to string
     */
    fun parseDateToString(date: Long): String {
        return SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(Date(date))
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
     * Get user accounts with JWT
     *
     * @param token user JWT
     */
    private fun getAccounts(token: String) {
        _getAccountsState.value = GetAccountsState(isLoading = true)
        getAccountsUseCase(token).onEach { result ->
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
     * Create TransactionDto instance
     *
     * @param accountName name of account
     * @param category name of category
     * @param value value of transaction
     * @param date date of transaction
     * @param toFromWhom to from/whom information
     * @param note note of transaction
     */
    fun createTransactionDto(accountName: String, category: String, value: Double, date: String, toFromWhom: String, note: String): TransactionDto? {
        return try {
            val accountId = _getAccountsState.value?.accounts?.first { it.name == accountName }?.accountId ?: -1
            TransactionDto(
                transactionId = null,
                accountId = accountId,
                category = category,
                value = value,
                date = parseStringToDate(date),
                toFromWhom = toFromWhom.ifBlank { null },
                note = note.ifBlank { null },
            )
        } catch (e: NoSuchElementException) {
            null
        }
    }

    /**
     * Create transaction with provided TransactionDto
     *
     * @param transactionDto TransactionDto instance
     */
    fun createTransaction(transactionDto: TransactionDto) {
        AppPreferences.jwtToken?.let { token ->
            createTransaction(token, transactionDto)
        } ?: run {
            _createTransactionState.value = CreateTransactionState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Create transaction with JWT and TransactionDto
     *
     * @param token user JWT
     * @param transactionDto TransactionDto instance
     */
    private fun createTransaction(token: String, transactionDto: TransactionDto) {
        _createTransactionState.value = CreateTransactionState(isLoading = true)
        createTransactionUseCase(token, transactionDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _createTransactionState.value = CreateTransactionState()
                }
                is Resource.Error -> {
                    _createTransactionState.value = CreateTransactionState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _createTransactionState.value = CreateTransactionState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}