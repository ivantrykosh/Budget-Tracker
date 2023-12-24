package com.ivantrykosh.app.budgettracker.client.presentation.main.report.category_report

import android.graphics.Color
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates.GetTransactionsBetweenDates
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.AccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.TransactionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.NoSuchElementException
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class CategoryReportViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsBetweenDatesUseCase: GetTransactionsBetweenDates,
) : ViewModel() {

    private val _getAccountsState = mutableStateOf(AccountsState())
    val getAccountsState: State<AccountsState> = _getAccountsState

    private val _isLoadingGetAccounts = MutableLiveData<Boolean>(false)
    val isLoadingGetAccounts: LiveData<Boolean> = _isLoadingGetAccounts

    private val _getTransactionsState = mutableStateOf(TransactionsState())
    val getTransactionsState: State<TransactionsState> = _getTransactionsState

    private val _isLoadingGetTransactions = MutableLiveData<Boolean>(false)
    val isLoadingGetTransactions: LiveData<Boolean> = _isLoadingGetTransactions

    private val _dateRange = MutableLiveData(Pair(Date(), Date()))
    val dateRange: LiveData<String> = _dateRange.map { range ->
        SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(range.first) +
        " - " +
        SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(range.second)
    }

    private var _maxCategoryValue = 0f
    val maxCategoryValue
        get() = _maxCategoryValue * 1.1f


    fun updateDateRange(firstDate: Date, secondDate: Date) {
        _dateRange.value = Pair(firstDate, secondDate)
    }

    private fun reformatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    fun getAccountIdByName(accountName: String): Long? {
        return try {
            _getAccountsState.value.accounts.first { it.name == accountName }.accountId
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

    fun getTransactions(accountIds: List<Long>, type: String) {
        AppPreferences.jwtToken?.let { token ->
            getTransactions(token, accountIds, reformatDate(_dateRange.value?.first ?: Date()), reformatDate(_dateRange.value?.second ?: Date()), type)
        } ?: run {
            _getAccountsState.value = AccountsState(error = "No JWT token found")
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

    private fun getTransactions(token: String, accountIds: List<Long>, startDate: String, endDate: String, type: String) {
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
                    }?.filter {
                        when (type) {
                            "Incomes" -> { it.value > 0 }
                            "Expenses" -> { it.value < 0 }
                            else -> { true }
                        }
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

    fun getBarDataByCategory(): BarData {
        _maxCategoryValue = 0.0f
        val transactions = _getTransactionsState.value.transactions

        // Group transactions by category
        val groupedByCategory = transactions.groupBy { it.category }

        var index = 0

        val barDataSets = groupedByCategory.map { (category, categoryTransactions) ->
            val sumOfTransactions = categoryTransactions.sumOf { it.value }.toFloat()
            _maxCategoryValue = _maxCategoryValue.coerceAtLeast(sumOfTransactions.absoluteValue)
            val barEntry = listOf(BarEntry(index.toFloat(), sumOfTransactions))
            index++
            val barDataSet = BarDataSet(barEntry, category)
            barDataSet.color = getRandomColor()
                barDataSet
        }

        return BarData(barDataSets)
    }

    private fun getRandomColor(): Int {
        return Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
    }
}