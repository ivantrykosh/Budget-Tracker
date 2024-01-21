package com.ivantrykosh.app.budgettracker.client.presentation.main.report.time_report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates.GetTransactionsBetweenDates
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.GetTransactionsState
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

/**
 * Time report view model
 */
@HiltViewModel
class TimeReportViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsBetweenDatesUseCase: GetTransactionsBetweenDates,
) : ViewModel() {

    /**
     * Time periods
     */
    enum class Period { DAY, WEEK, MONTH, YEAR }

    private val _getAccountsState = MutableLiveData(GetAccountsState())
    val getAccountsState: LiveData<GetAccountsState> = _getAccountsState

    private val _getTransactionsState = MutableLiveData(GetTransactionsState())
    val getTransactionsState: LiveData<GetTransactionsState> = _getTransactionsState

    private val _dateRange = MutableLiveData(Pair(getStartOfDay(Date()), getEndOfDay(Date())))
    val dateRange: LiveData<String> = _dateRange.map { range ->
        SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(range.first) +
                " - " +
                SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(range.second)
    }

    private var period: Period = Period.DAY

    private var _labels = mutableSetOf<String>()
    val labels
        get() = _labels

    private var _maxTimeValue = 0f
    val maxTimeValue
        get() = _maxTimeValue * 1.1f

    /**
     * Get start of day
     */
    private fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    /**
     * Get end of day
     */
    private fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        return calendar.time
    }

    /**
     * Set period
     *
     * @param period time period
     */
    fun setPeriod(period: Period) {
        this.period = period
        _dateRange.value = getDates(getStartOfDay(_dateRange.value?.first ?: Date()), getEndOfDay(_dateRange.value?.second ?: Date()))
    }

    /**
     * Update date range
     *
     * @param firstDate first date
     * @param secondDate second date
     */
    fun updateDateRange(firstDate: Date, secondDate: Date) {
        _dateRange.value = getDates(getStartOfDay(firstDate), getEndOfDay(secondDate))
    }

    /**
     * Get pair of dates and period
     *
     * @param firstDate first date
     * @param secondDate second date
     */
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

    /**
     * Parse date to user format string
     *
     * @param date date to parse
     */
    private fun userFormatDate(date: Date): String {
        return SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(date)
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
     * Get account
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
     * Get transaction using account IDs, start and end date and type
     *
     * @param accountIds IDs of accounts
     * @param type transaction type
     */
    fun getTransactions(accountIds: List<Long>, type: Int) {
        _getTransactionsState.value = GetTransactionsState(isLoading = true)
        getTransactionsBetweenDatesUseCase(accountIds, _dateRange.value!!.first, _dateRange.value!!.second).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    val transactions = result.data?.filter {
                        when {
                            type > 0 -> { it.value > 0 }
                            type < 0 -> { it.value < 0 }
                            else -> { true }
                        }
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
     * Get line data
     */
    fun getLineData(): LineData {
        val transactions = getTransactionsState.value?.transactions ?: emptyList()

        val entries = mutableListOf<Entry>()

        // Create a map to store the sum of transactions for each date
        val sumMap = initializeSumMap()


        // Iterate through transactions and calculate the sum for each date within the selected period
        transactions.forEach { transaction ->
            val dateKey = when (period) {
                Period.DAY -> userFormatDate(transaction.date)
                Period.WEEK -> userFormatDate(getWeekEndDate(transaction.date))
                Period.MONTH -> userFormatDate(getMonthEndDate(transaction.date))
                Period.YEAR -> userFormatDate(getYearEndDate(transaction.date))
            }

            // Update the sum for the date
            sumMap[dateKey] = (sumMap[dateKey] ?: 0f) + transaction.value.toFloat()
        }

        // Convert the map entries to MPAndroidChart entries
        sumMap.entries.forEachIndexed { index, entry ->
            val sum = entry.value
            _maxTimeValue = _maxTimeValue.coerceAtLeast(sum.absoluteValue)
            entries.add(Entry(index.toFloat(), sum))
        }

        _labels = sumMap.keys

        // Create LineDataSet
        val dataSet = LineDataSet(entries, "Transaction Sum")

        return LineData(dataSet)
    }

    /**
     * Initialize sum with 0
     */
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
                    Period.DAY -> userFormatDate(calendar.time)
                    Period.WEEK -> userFormatDate(getWeekEndDate(calendar.time))
                    Period.MONTH -> userFormatDate(getMonthEndDate(calendar.time))
                    Period.YEAR -> userFormatDate(getYearEndDate(calendar.time))
                }

                sumMap[dateKey] = 0f

                calendar.add(getCalendarFieldForPeriod(), 1)
            }
        }

        return sumMap
    }

    /**
     * Get calendar field for period
     */
    private fun getCalendarFieldForPeriod(): Int {
        return when (period) {
            Period.DAY -> Calendar.DAY_OF_MONTH
            Period.WEEK -> Calendar.WEEK_OF_YEAR
            Period.MONTH -> Calendar.MONTH
            Period.YEAR -> Calendar.YEAR
        }
    }

    /**
     * Get end date of week
     *
     * @param date date
     */
    private fun getWeekEndDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
        return calendar.time
    }

    /**
     * Get end date of month
     *
     * @param date date
     */
    private fun getMonthEndDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return calendar.time
    }

    /**
     * Get end date of year
     *
     * @param date date
     */
    private fun getYearEndDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        return calendar.time
    }
}