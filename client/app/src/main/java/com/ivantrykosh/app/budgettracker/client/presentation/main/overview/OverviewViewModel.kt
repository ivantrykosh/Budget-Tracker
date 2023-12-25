package com.ivantrykosh.app.budgettracker.client.presentation.main.overview

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates.GetTransactionsBetweenDates
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.get_user.GetUserUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.AccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.TransactionsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.user.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsBetweenDates: GetTransactionsBetweenDates,
) : ViewModel() {

    private val _getUserState = mutableStateOf(UserState())
    val getUserState: State<UserState> = _getUserState

    private val _isLoadingGetUser = MutableLiveData<Boolean>(false)
    val isLoadingGetUser: LiveData<Boolean> = _isLoadingGetUser

    private val _getAccountsState = mutableStateOf(AccountsState())
    val getAccountsState: State<AccountsState> = _getAccountsState

    private val _isLoadingGetAccounts = MutableLiveData<Boolean>(false)
    val isLoadingGetAccounts: LiveData<Boolean> = _isLoadingGetAccounts

    private val _getTransactionsState = mutableStateOf(TransactionsState())
    val getTransactionsState: State<TransactionsState> = _getTransactionsState

    private val _isLoadingGetTransactions = MutableLiveData<Boolean>(false)
    val isLoadingGetTransactions: LiveData<Boolean> = _isLoadingGetTransactions

    fun getStartMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    fun getEndMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
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

    fun getTransactions(accountIds: List<Long>, startDate: String, endDate: String) {
        AppPreferences.jwtToken?.let { token ->
            getTransactions(token, accountIds, startDate, endDate)
        } ?: run {
            _getTransactionsState.value = TransactionsState(error = "No JWT token found")
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

    private fun getTransactions(token: String, accountIds: List<Long>, startDate: String, endDate: String) {
        _isLoadingGetTransactions.value = true
        getTransactionsBetweenDates(token, accountIds, startDate, endDate).onEach {result ->
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

    fun getIncomes(): List<Transaction> {
        return getTransactionsState.value.transactions.filter { it.value > 0.0 }
    }

    fun getExpenses(): List<Transaction> {
        return getTransactionsState.value.transactions.filter { it.value < 0.0 }
    }

    fun getSumOfIncomes(): Double {
        return getIncomes().sumOf { it.value }
    }
    fun getSumOfExpenses(): Double {
        return getExpenses().sumOf { it.value }
    }
    fun getTotalSum(): Double {
        return getTransactionsState.value.transactions.sumOf { it.value }
    }
}