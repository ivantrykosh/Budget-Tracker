package com.ivantrykosh.app.budgettracker.client.presentation.main.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.util.Calendar
import java.util.Locale
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
    fun getStartMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Get end of month
     */
    fun getEndMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Get all accounts
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
     * Get accounts with JWT
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
     * Get transactions with JWT, account IDs and between dates
     *
     * @param token user JWT
     * @param accountIds IDs of accounts
     * @param startDate start date
     * @param endDate end date
     */
    private fun getTransactions(token: String, accountIds: List<Long>, startDate: String, endDate: String) {
        _getTransactionsState.value = GetTransactionsState(isLoading = true)
        getTransactionsBetweenDates(token, accountIds, startDate, endDate).onEach {result ->
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
     * Get incomes
     */
    fun getIncomes(): List<Transaction> {
        return getTransactionsState.value?.transactions?.filter { it.value > 0.0 } ?: emptyList()
    }

    /**
     * Get expenses
     */
    fun getExpenses(): List<Transaction> {
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