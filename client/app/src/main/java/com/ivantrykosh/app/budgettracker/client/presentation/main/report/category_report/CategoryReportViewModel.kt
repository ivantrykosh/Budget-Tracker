package com.ivantrykosh.app.budgettracker.client.presentation.main.report.category_report

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates.GetTransactionsBetweenDates
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.GetTransactionsState
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

    private val _getAccountsState = MutableLiveData(GetAccountsState())
    val getAccountsState: LiveData<GetAccountsState> = _getAccountsState

    private val _getTransactionsState = MutableLiveData(GetTransactionsState())
    val getTransactionsState: LiveData<GetTransactionsState> = _getTransactionsState

    private val _dateRange = MutableLiveData(Pair(Date(), Date()))
    val dateRange: LiveData<String> = _dateRange.map { range ->
        SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(range.first) +
        " - " +
        SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(range.second)
    }

    private var _maxCategoryValue = 0f
    val maxCategoryValue
        get() = _maxCategoryValue * 1.1f


    /**
     * Update date range
     *
     * @param firstDate first date
     * @param secondDate second date
     */
    fun updateDateRange(firstDate: Date, secondDate: Date) {
        _dateRange.value = Pair(firstDate, secondDate)
    }

    /**
     * Parse date to string
     *
     * @param date date to parse
     */
    private fun reformatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    /**
     * Get account ID by its name
     *
     * @param accountName name of account
     */
    fun getAccountIdByName(accountName: String): Long? {
        return try {
            _getAccountsState.value?.accounts?.first { it.name == accountName }?.accountId ?: -1
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
     * Get transaction by account IDs and type
     *
     * @param accountIds IDs of accounts
     * @param type type of transactions
     */
    fun getTransactions(accountIds: List<Long>, type: Int) {
        AppPreferences.jwtToken?.let { token ->
            getTransactions(token, accountIds, reformatDate(_dateRange.value?.first ?: Date()), reformatDate(_dateRange.value?.second ?: Date()), type)
        } ?: run {
            _getTransactionsState.value = GetTransactionsState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Get account using JWT
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
     * Get transaction using JWT and account IDs, start and end date and type
     *
     * @param token user JWT
     * @param accountIds IDs of accounts
     * @param startDate start date
     * @param endDate end date
     * @param type transaction type
     */
    private fun getTransactions(token: String, accountIds: List<Long>, startDate: String, endDate: String, type: Int) {
        _getTransactionsState.value = GetTransactionsState(isLoading = true)
        getTransactionsBetweenDatesUseCase(token, accountIds, startDate, endDate).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    val transactions = result.data?.filter {
                        when {
                            type > 0 -> { it.value > 0 }
                            type < 0 -> { it.value < 0 }
                            else -> { true }
                        }
                    }?.map { transactionDto ->
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
     * Get bar data by categories
     */
    fun getBarDataByCategory(): BarData {
        _maxCategoryValue = 0.0f
        val transactions = _getTransactionsState.value?.transactions ?: emptyList()

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

    /**
     * Get random color
     */
    private fun getRandomColor(): Int {
        return Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
    }
}