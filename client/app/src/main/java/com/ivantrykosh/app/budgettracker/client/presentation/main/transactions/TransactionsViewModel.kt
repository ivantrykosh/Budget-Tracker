package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.model.TransactionDetails
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.delete_transaction.DeleteTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transaction.GetTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates.GetTransactionsBetweenDates
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.update_transaction.UpdateTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.DeleteTransactionState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.GetTransactionState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.GetTransactionsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.UpdateTransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.NoSuchElementException
import javax.inject.Inject

/**
 * Transactions view model
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsBetweenDatesUseCase: GetTransactionsBetweenDates,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _getAccountsState = MutableLiveData(GetAccountsState())
    val getAccountsState: LiveData<GetAccountsState> = _getAccountsState

    private val _getTransactionsState = MutableLiveData(GetTransactionsState())
    val getTransactionsState: LiveData<GetTransactionsState> = _getTransactionsState

    private val _getTransactionState = MutableLiveData(GetTransactionState())
    val getTransactionState: LiveData<GetTransactionState> = _getTransactionState

    private val _updateTransactionState = MutableLiveData(UpdateTransactionState())
    val updateTransactionState: LiveData<UpdateTransactionState> = _updateTransactionState

    private val _deleteTransactionState = MutableLiveData(DeleteTransactionState())
    val deleteTransactionState: LiveData<DeleteTransactionState> = _deleteTransactionState

    private val _currentDate = MutableLiveData(Date())
    val currentDate: LiveData<String> = _currentDate.map { date ->
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
    }

    /**
     * Parse date to string
     */
    fun reformatDate(date: Date): String {
        return SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(date)
    }

    /**
     * Update current date
     */
    fun updateCurrentDate() {
        _currentDate.value = Date()
    }

    /**
     * Minus month from current date
     */
    fun minusMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.add(Calendar.MONTH, -1)
        _currentDate.value = calendar.time
    }

    /**
     * Plus month to current date
     */
    fun plusMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.add(Calendar.MONTH, 1)
        _currentDate.value = calendar.time
    }

    /**
     * Get start of month
     */
    fun getStartMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Get end of month
     */
    fun getEndMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

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
    fun parseStringToDate(date: String): Date {
        val inputFormat = SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault())
        return inputFormat.parse(date) ?: Date()
    }

    /**
     * To TransactionDto instance
     *
     * @param id ID of transaction
     * @param accountName name of account
     * @param category name of category
     * @param value value of transaction
     * @param date date of transaction
     * @param toFromWhom to from/whom information
     * @param note note of transaction
     */
    fun toTransactionDto(id: Long?, accountName: String, category: String, value: Double, date: Date, toFromWhom: String, note: String): TransactionDto? {
        return try {
            val accountId = _getAccountsState.value?.accounts?.first { it.name == accountName }?.accountId ?: -1
            TransactionDto(
                transactionId = id,
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
     * Get transactions between dates and with provided account IDs
     *
     * @param accountIds IDs of accounts
     * @param startDate start date
     * @param endDate end date
     */
    fun getTransactions(accountIds: List<Long>, startDate: String, endDate: String) {
        AppPreferences.jwtToken?.let { token ->
            getTransactions(token, accountIds, startDate, endDate)
        } ?: run {
            _getTransactionsState.value = GetTransactionsState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Get transaction with ID
     *
     * @param id ID of transaction
     */
    fun getTransaction(id: String) {
        AppPreferences.jwtToken?.let { token ->
            getTransaction(token, id)
        } ?: run {
            _getTransactionState.value = GetTransactionState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Update transaction with TransactionDto
     *
     * @param transactionDto TransactionDto instance
     */
    fun updateTransaction(transactionDto: TransactionDto) {
        AppPreferences.jwtToken?.let { token ->
            updateTransaction(token, transactionDto)
        } ?: run {
            _updateTransactionState.value = UpdateTransactionState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Delete transaction with ID
     *
     * @param id ID of transaction
     */
    fun deleteTransaction(id: String) {
        AppPreferences.jwtToken?.let { token ->
            deleteTransaction(token, id)
        } ?: run {
            _deleteTransactionState.value = DeleteTransactionState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Get accounts with JWT
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
     * Get transactions with JWT, account IDs and between dates
     *
     * @param token user JWT
     * @param accountIds IDs of accounts
     * @param startDate start date
     * @param endDate end date
     */
    private fun getTransactions(token: String, accountIds: List<Long>, startDate: String, endDate: String) {
        _getTransactionsState.value = GetTransactionsState(isLoading = true)
        getTransactionsBetweenDatesUseCase(token, accountIds, startDate, endDate).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    val transactions = result.data?.map { transactionDto ->
                        Transaction(
                            transactionId = transactionDto.transactionId,
                            accountName = _getAccountsState.value!!.accounts.filter { it.accountId == transactionDto.accountId }.map { it.name }.first(),
                            category = transactionDto.category,
                            value = transactionDto.value,
                            date = transactionDto.date
                        )
                    } ?: emptyList()
                    _getTransactionsState.value = GetTransactionsState(transactions = transactions)
                }
                is Resource.Error -> {
                    _getTransactionsState.value = GetTransactionsState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _getTransactionsState.value = GetTransactionsState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Get transaction with JWT and its ID
     *
     * @param token user JWT
     * @param id ID of transaction
     */
    private fun getTransaction(token: String, id: String) {
        _getTransactionState.value = GetTransactionState(isLoading = true)
        getTransactionUseCase(token, id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val transaction = result.data?.let { transactionDto ->
                        TransactionDetails(
                            transactionId = transactionDto.transactionId,
                            accountName = _getAccountsState.value!!.accounts.filter { it.accountId == transactionDto.accountId }.map { it.name }.first(),
                            category = transactionDto.category,
                            value = transactionDto.value,
                            date = transactionDto.date,
                            toFromWhom = transactionDto.toFromWhom,
                            note = transactionDto.note
                        )
                    }
                    _getTransactionState.value = GetTransactionState(transaction = transaction)
                }
                is Resource.Error -> {
                    _getTransactionState.value = GetTransactionState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _getTransactionState.value = GetTransactionState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Update transaction with JWT and TransactionDto
     *
     * @param token user JWT
     * @param transactionDto TransactionDto instance
     */
    private fun updateTransaction(token: String, transactionDto: TransactionDto) {
        _updateTransactionState.value = UpdateTransactionState(isLoading = true)
        updateTransactionUseCase(token, transactionDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _updateTransactionState.value = UpdateTransactionState()
                }
                is Resource.Error -> {
                    _updateTransactionState.value = UpdateTransactionState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _updateTransactionState.value = UpdateTransactionState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Delete transaction with JWT and its ID
     *
     * @param token user JWT
     * @param id ID of transaction
     */
    private fun deleteTransaction(token: String, id: String) {
        _deleteTransactionState.value = DeleteTransactionState(isLoading = true)
        deleteTransactionUseCase(token, id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _deleteTransactionState.value = DeleteTransactionState()
                }
                is Resource.Error -> {
                    _deleteTransactionState.value = DeleteTransactionState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _deleteTransactionState.value = DeleteTransactionState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}