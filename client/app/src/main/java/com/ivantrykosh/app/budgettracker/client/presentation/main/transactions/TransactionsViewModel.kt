package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
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

    private val _currentTransactionType = MutableLiveData(0)
    val currentTransactionType: LiveData<Int> = _currentTransactionType

    private val _currentAccount = MutableLiveData<String?>(null)
    val currentAccount: LiveData<String?> = _currentAccount

    private val _isDateChecked = MutableLiveData(true)
    val isDateChecked: LiveData<Boolean> = _isDateChecked

    private val _isValueChecked = MutableLiveData(false)
    val isValueChecked: LiveData<Boolean> = _isValueChecked


    /**
     * Set current transaction type for filter dialog
     *
     * @param type transaction type
     */
    fun setTransactionType(type: Int) {
        _currentTransactionType.value = type
    }

    /**
     * Set current account for filter dialog
     *
     * @param account account. If null, then all accounts are selected
     */
    fun setAccount(account: String?) {
        _currentAccount.value = account
    }

    /**
     * Set date checking for filter dialog
     *
     * @param isDateChecked is date checked
     */
    fun setIsDateChecked(isDateChecked: Boolean) {
        _isDateChecked.value = isDateChecked
    }

    /**
     * Set value checking for filter dialog
     *
     * @param isValueChecked is value checked
     */
    fun setIsValueChecked(isValueChecked: Boolean) {
        _isValueChecked.value = isValueChecked
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
    fun getStartMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    /**
     * Get end of month
     */
    fun getEndMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        return calendar.time
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
    fun toTransaction(id: Long, accountName: String, category: String, value: Double, date: Date, toFromWhom: String, note: String): Transaction? {
        return try {
            val accountId = _getAccountsState.value?.accounts?.first { it.name == accountName }?.accountId ?: -1
            Transaction(
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

    fun getAccountNameById(accountId: Long): String {
        return _getAccountsState.value?.accounts?.first { it.accountId == accountId }?.name ?: ""
    }

    /**
     * Get accounts with JWT
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
     * Get transactions with JWT, account IDs and between dates
     *
     * @param accountIds IDs of accounts
     * @param startDate start date
     * @param endDate end date
     */
    fun getTransactions(accountIds: List<Long>, startDate: Date, endDate: Date) {
        _getTransactionsState.value = GetTransactionsState(isLoading = true)
        getTransactionsBetweenDatesUseCase(accountIds, startDate, endDate).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getTransactionsState.value = GetTransactionsState(transactions = filterTransactions(result.data ?: emptyList()))
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
     * Filter transactions by type and account, sort by date and value
     *
     * @param transactions transactions to sort
     */
    private fun filterTransactions(transactions: List<SubTransaction>): List<SubTransaction> {
        val transactionsFilterByTypes = when {
            (_currentTransactionType.value ?: 0) > 0 -> transactions.filter { it.value > 0 }
            (_currentTransactionType.value ?: 0) < 0 -> transactions.filter { it.value < 0 }
            else -> transactions
        }
        val transactionsFilterByAccount = when (_currentAccount.value) {
            null -> transactionsFilterByTypes
            else -> transactionsFilterByTypes.filter { it.name == _currentAccount.value }
        }
        val transactionSortByDate = when (_isDateChecked.value) {
            true -> transactionsFilterByAccount.sortedByDescending { it.date }
            else -> transactionsFilterByAccount
        }
        val transactionSortByValue = when (_isValueChecked.value) {
            true -> transactionSortByDate.sortedByDescending { it.value }
            else -> transactionSortByDate
        }
        return transactionSortByValue
    }

    /**
     * Get transaction with JWT and its ID
     * @param id ID of transaction
     */
    fun getTransaction(id: Long) {
        _getTransactionState.value = GetTransactionState(isLoading = true)
        getTransactionUseCase(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _getTransactionState.value = GetTransactionState(transaction = result.data)
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
     * @param transaction TransactionDto instance
     */
    fun updateTransaction(transaction: Transaction) {
        _updateTransactionState.value = UpdateTransactionState(isLoading = true)
        updateTransactionUseCase(transaction).onEach { result ->
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
     * @param id ID of transaction
     */
    fun deleteTransaction(id: Long) {
        _deleteTransactionState.value = DeleteTransactionState(isLoading = true)
        deleteTransactionUseCase(id).onEach { result ->
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