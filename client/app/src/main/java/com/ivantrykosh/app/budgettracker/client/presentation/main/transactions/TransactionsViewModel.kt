package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.model.TransactionDetails
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.delete_transaction.DeleteTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transaction.GetTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates.GetTransactionsBetweenDates
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.update_transaction.UpdateTransactionUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.AccountsState
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

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsBetweenDatesUseCase: GetTransactionsBetweenDates,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _getAccountsState = mutableStateOf(AccountsState())
    val getAccountsState: State<AccountsState> = _getAccountsState

    private val _isLoadingGetAccounts = MutableLiveData<Boolean>(false)
    val isLoadingGetAccounts: LiveData<Boolean> = _isLoadingGetAccounts

    private val _getTransactionsState = mutableStateOf(TransactionsState())
    val getTransactionsState: State<TransactionsState> = _getTransactionsState

    private val _isLoadingGetTransactions = MutableLiveData<Boolean>(false)
    val isLoadingGetTransactions: LiveData<Boolean> = _isLoadingGetTransactions

    private val _getTransactionState = mutableStateOf(GetTransactionState())
    val getTransactionState: State<GetTransactionState> = _getTransactionState

    private val _isLoadingGetTransaction = MutableLiveData<Boolean>(false)
    val isLoadingGetTransaction: LiveData<Boolean> = _isLoadingGetTransaction

    private val _updateTransactionState = mutableStateOf(UpdateTransactionState())
    val updateTransactionState: State<UpdateTransactionState> = _updateTransactionState

    private val _isLoadingUpdateTransaction = MutableLiveData<Boolean>(false)
    val isLoadingUpdateTransaction: LiveData<Boolean> = _isLoadingUpdateTransaction

    private val _deleteTransactionState = mutableStateOf(DeleteTransactionState())
    val deleteTransactionState: State<DeleteTransactionState> = _deleteTransactionState

    private val _isLoadingDeleteTransaction = MutableLiveData<Boolean>(false)
    val isLoadingDeleteTransaction: LiveData<Boolean> = _isLoadingDeleteTransaction

    private val _currentDate = MutableLiveData(Date())
    val currentDate: LiveData<String> = _currentDate.map { date ->
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
    }

    fun reformatDate(date: Date): String {
        return SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(date)
    }

    fun updateCurrentDate() {
        _currentDate.value = Date()
    }

    fun minusMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.add(Calendar.MONTH, -1)
        _currentDate.value = calendar.time
    }

    fun plusMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.add(Calendar.MONTH, 1)
        _currentDate.value = calendar.time
    }

    fun getStartMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    fun getEndMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

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
        val inputFormat = SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault())
        return inputFormat.parse(date) ?: Date()
    }

    fun toTransactionDto(id: Long?, accountName: String, category: String, value: Double, date: Date, toFromWhom: String, note: String): TransactionDto? {
        return try {
            val accountId = _getAccountsState.value.accounts.first { it.name == accountName }.accountId
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

    fun getAccounts() {
        AppPreferences.jwtToken?.let { token ->
            getAccounts(token)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    fun getTransactions(accountIds: List<Long>, startDate: String, endDate: String) {
        AppPreferences.jwtToken?.let { token ->
            getTransactions(token, accountIds, startDate, endDate)
        } ?: run {
            _getTransactionsState.value = TransactionsState(error = "No JWT token found")
        }
    }

    fun getTransaction(id: String) {
        AppPreferences.jwtToken?.let { token ->
            getTransaction(token, id)
        } ?: run {
            _getTransactionState.value = GetTransactionState(error = "No JWT token found")
        }
    }

    fun updateTransaction(transactionDto: TransactionDto) {
        AppPreferences.jwtToken?.let { token ->
            updateTransaction(token, transactionDto)
        } ?: run {
            _updateTransactionState.value = UpdateTransactionState(error = "No JWT token found")
        }
    }

    fun deleteTransaction(id: String) {
        AppPreferences.jwtToken?.let { token ->
            deleteTransaction(token, id)
        } ?: run {
            _deleteTransactionState.value = DeleteTransactionState(error = "No JWT token found")
        }
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

    private fun getTransactions(token: String, accountIds: List<Long>, startDate: String, endDate: String) {
        _isLoadingGetTransactions.value = true
        getTransactionsBetweenDatesUseCase(token, accountIds, startDate, endDate).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    val transactions = result.data?.map { transactionDto ->
                        Transaction(
                            transactionId = transactionDto.transactionId,
                            accountName = _getAccountsState.value.accounts.filter { it.accountId == transactionDto.accountId }.map { it.name }.first(),
                            category = transactionDto.category,
                            value = transactionDto.value,
                            date = transactionDto.date
                        )
                    } ?: emptyList()
                    _getTransactionsState.value = TransactionsState(transactions = transactions)
                    _isLoadingGetTransactions.value = false
                }
                is Resource.Error -> {
                    _getTransactionsState.value = TransactionsState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingGetTransactions.value = false
                }
                is Resource.Loading -> {
                    _getTransactionsState.value = TransactionsState(isLoading = true)
                    _isLoadingGetTransactions.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getTransaction(token: String, id: String) {
        _isLoadingGetTransaction.value = true
        getTransactionUseCase(token, id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val transaction = result.data?.let { transactionDto ->
                        TransactionDetails(
                            transactionId = transactionDto.transactionId,
                            accountName = _getAccountsState.value.accounts.filter { it.accountId == transactionDto.accountId }.map { it.name }.first(),
                            category = transactionDto.category,
                            value = transactionDto.value,
                            date = transactionDto.date,
                            toFromWhom = transactionDto.toFromWhom,
                            note = transactionDto.note
                        )
                    }
                    _getTransactionState.value = GetTransactionState(transaction = transaction)
                    _isLoadingGetTransaction.value = false
                }
                is Resource.Error -> {
                    _getTransactionState.value = GetTransactionState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingGetTransaction.value = false
                }
                is Resource.Loading -> {
                    _getTransactionState.value = GetTransactionState(isLoading = true)
                    _isLoadingGetTransaction.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun updateTransaction(token: String, transactionDto: TransactionDto) {
        _isLoadingUpdateTransaction.value = true
        updateTransactionUseCase(token, transactionDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _updateTransactionState.value = UpdateTransactionState()
                    _isLoadingUpdateTransaction.value = false
                }
                is Resource.Error -> {
                    _updateTransactionState.value = UpdateTransactionState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingUpdateTransaction.value = false
                }
                is Resource.Loading -> {
                    _updateTransactionState.value = UpdateTransactionState(isLoading = true)
                    _isLoadingUpdateTransaction.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun deleteTransaction(token: String, id: String) {
        _isLoadingDeleteTransaction.value = true
        deleteTransactionUseCase(token, id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _deleteTransactionState.value = DeleteTransactionState()
                    _isLoadingDeleteTransaction.value = false
                }
                is Resource.Error -> {
                    _deleteTransactionState.value = DeleteTransactionState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingDeleteTransaction.value = false
                }
                is Resource.Loading -> {
                    _deleteTransactionState.value = DeleteTransactionState(isLoading = true)
                    _isLoadingDeleteTransaction.value = true
                }
            }
        }.launchIn(viewModelScope)
    }
}