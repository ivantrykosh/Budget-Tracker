package com.ivantrykosh.app.budgettracker.client.presentation.main.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.get_accounts.GetAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.transaction.get_transactions_between_dates.GetTransactionsBetweenDates
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state.GetTransactionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Overview view model
 */
@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsBetweenDates: GetTransactionsBetweenDates,
) : ViewModel() {

    private val _getAccountsState = MutableLiveData(GetAccountsState())
    val getAccountsState: LiveData<GetAccountsState> = _getAccountsState

    private val _getTransactionsState = MutableLiveData(GetTransactionsState())
    val getTransactionsState: LiveData<GetTransactionsState> = _getTransactionsState

    /**
     * Get start of month
     */
    fun getStartMonth(): Date {
        val calendar = Calendar.getInstance()
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
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        return calendar.time
    }

    /**
     * Get accounts
     *
     */
    fun getAccounts() {
        _getAccountsState.value = GetAccountsState(isLoading = true)
        getAccountsUseCase().onEach { result ->
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
     * Get transactions with account IDs and between dates
     *
     * @param accountIds IDs of accounts
     * @param startDate start date
     * @param endDate end date
     */
    fun getTransactions(accountIds: List<Long>, startDate: Date, endDate: Date) {
        _getTransactionsState.value = GetTransactionsState(isLoading = true)
        getTransactionsBetweenDates(accountIds, startDate, endDate).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getTransactionsState.value = GetTransactionsState(transactions = result.data ?: emptyList())
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
     * Get format of values
     */
    fun getFormat(): DecimalFormat {
        val pattern = Constants.CURRENCIES[AppPreferences.currency] + "#,##0.00"
        val format = DecimalFormat(pattern)
        format.maximumFractionDigits = 2
        return format
    }

    /**
     * Get incomes
     */
    fun getIncomes(): List<SubTransaction> {
        return getTransactionsState.value?.transactions?.filter { it.value > 0.0 } ?: emptyList()
    }

    /**
     * Get expenses
     */
    fun getExpenses(): List<SubTransaction> {
        return getTransactionsState.value?.transactions?.filter { it.value < 0.0 } ?: emptyList()
    }

    /**
     * Get sum of incomes
     */
    fun getSumOfIncomes(): Double {
        return getIncomes().sumOf { it.value }
    }

    /**
     * Get sum of expenses
     */
    fun getSumOfExpenses(): Double {
        return getExpenses().sumOf { it.value }
    }

    /**
     * Get total sum
     */
    fun getTotalSum(): Double {
        return getTransactionsState.value?.transactions?.sumOf { it.value } ?: 0.0
    }
}