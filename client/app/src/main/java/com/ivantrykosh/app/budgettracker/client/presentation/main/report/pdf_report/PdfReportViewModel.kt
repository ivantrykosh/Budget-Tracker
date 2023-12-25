package com.ivantrykosh.app.budgettracker.client.presentation.main.report.pdf_report

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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.NoSuchElementException
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class PdfReportViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsBetweenDatesUseCase: GetTransactionsBetweenDates,
) : ViewModel() {
    enum class Period { DAY, WEEK, MONTH, YEAR }

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

    private var period: Period = Period.DAY

    private var _labels = mutableSetOf<String>()
    val labels
        get() = _labels

    private var _maxCategoryValue = 0f
    val maxCategoryValue
        get() = _maxCategoryValue * 1.15f

    private var _maxTimeValue = 0f
    val maxTimeValue
        get() = _maxTimeValue * 1.15f


    fun setPeriod(period: Period) {
        this.period = period
        _dateRange.value = getDates(_dateRange.value?.first ?: Date(), _dateRange.value?.second ?: Date())
    }

    fun updateDateRange(firstDate: Date, secondDate: Date) {
        _dateRange.value = getDates(firstDate, secondDate)
    }

    private fun getDates(firstDate: Date, secondDate: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.time = firstDate
        val firstPeriodDate = when (period) {
            Period.DAY -> {
                calendar.time
            }
            Period.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.time
            }
            Period.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.time
            }
            Period.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.time
            }
        }

        calendar.time = secondDate
        val secondPeriodDate = when (period) {
            Period.DAY -> {
                calendar.time
            }
            Period.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
                calendar.time
            }
            Period.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.time
            }
            Period.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                calendar.time
            }
        }
        return Pair(firstPeriodDate, secondPeriodDate)
    }

    private fun reformatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    fun userFormatDate(date: Date): String {
        return SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(date)
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
            _getTransactionsState.value = TransactionsState(error = "No JWT token found")
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

    fun getLineData(): LineData {
        val transactions = getTransactionsState.value.transactions

        val entries = mutableListOf<Entry>()

        // Create a map to store the sum of transactions for each date
        val sumMap = initializeSumMap()


        // Iterate through transactions and calculate the sum for each date within the selected period
        transactions.forEach { transaction ->
            val dateKey = when (period) {
                Period.DAY -> reformatDate(transaction.date)
                Period.WEEK -> reformatDate(getWeekEndDate(transaction.date))
                Period.MONTH -> reformatDate(getMonthEndDate(transaction.date))
                Period.YEAR -> reformatDate(getYearEndDate(transaction.date))
            }

            // Update the sum for the date
            sumMap[dateKey] = (sumMap[dateKey] ?: 0f) + transaction.value.toFloat()
        }

        // Convert the map entries to MPAndroidChart entries
        sumMap.entries.forEachIndexed { index, entry ->
            val date = entry.key
            val sum = entry.value
            _maxTimeValue = _maxTimeValue.coerceAtLeast(sum.absoluteValue)
            entries.add(Entry(index.toFloat(), sum))
        }

        _labels = sumMap.keys

        // Create LineDataSet
        val dataSet = LineDataSet(entries, "Transaction Sum")

        // Create LineData and return
        return LineData(dataSet)
    }

    private fun initializeSumMap(): MutableMap<String, Float> {
        val sumMap = mutableMapOf<String, Float>()

        // Initialize the map with default values based on the last date of each period in the date range
        val startDate = _dateRange.value?.first
        val endDate = _dateRange.value?.second

        if (startDate != null && endDate != null) {
            val calendar = Calendar.getInstance()
            calendar.time = startDate

            while (calendar.time <= endDate) {
                val dateKey = when (period) {
                    Period.DAY -> reformatDate(calendar.time)
                    Period.WEEK -> reformatDate(getWeekEndDate(calendar.time))
                    Period.MONTH -> reformatDate(getMonthEndDate(calendar.time))
                    Period.YEAR -> reformatDate(getYearEndDate(calendar.time))
                }

                sumMap[dateKey] = 0f

                // Move to the next date
                calendar.add(getCalendarFieldForPeriod(), 1)
            }
        }

        return sumMap
    }

    private fun getCalendarFieldForPeriod(): Int {
        return when (period) {
            Period.DAY -> Calendar.DAY_OF_MONTH
            Period.WEEK -> Calendar.WEEK_OF_YEAR
            Period.MONTH -> Calendar.MONTH
            Period.YEAR -> Calendar.YEAR
        }
    }

    private fun getWeekEndDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
        return calendar.time
    }

    private fun getMonthEndDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return calendar.time
    }

    private fun getYearEndDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        return calendar.time
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