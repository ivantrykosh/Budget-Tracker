package com.ivantrykosh.app.budgettracker.client.presentation.main.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.delete_all_accounts.DeleteAllAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state.GetAccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.settings.notification.DailyReminderReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Settings view model
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllAccountsUseCase: DeleteAllAccountsUseCase,
) : ViewModel() {

    private val _deleteAllAccountsState = MutableLiveData(GetAccountsState())
    val deleteAllAccountsState: LiveData<GetAccountsState> = _deleteAllAccountsState

    private val _currency = MutableLiveData(AppPreferences.currency)
    val currency: LiveData<String> = _currency.map {  currency ->
        currency?.let {
            it + " - " + Constants.CURRENCIES[it]
        } ?: ""
    }

    private val _dateFormat = MutableLiveData(AppPreferences.dateFormat)
    val dateFormat: LiveData<String> = _dateFormat.map {
        _dateFormat.value ?: ""
    }

    private val _dailyReminderTime = MutableLiveData(AppPreferences.reminderTime)
    val dailyReminderTime: LiveData<String?> = _dailyReminderTime.map {
        it ?: "-"
    }

    /**
     * Get currencies with their symbols
     */
    fun getCurrencies(): List<String> {
        return Constants.CURRENCIES.map {
            it.key + " - " + it.value
        }
    }

    /**
     * Get date formats
     */
    fun getDateFormats(): List<String> {
        return Constants.DATE_FORMATS.toList()
    }

    /**
     * Set daily reminder
     *
     * @param hours hour of reminder
     * @param minutes minute of reminder
     * @param locale locale
     */
    private fun setDailyReminderTime(hours: Int, minutes: Int, locale: Locale) {
        val time = LocalTime.of(hours, minutes)
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)

        val formattedTime = time.format(formatter)

        AppPreferences.reminderTime = formattedTime
        _dailyReminderTime.value = formattedTime
    }

    /**
     * Delete daily reminder
     */
    private fun deleteDailyReminderTime() {
        _dailyReminderTime.value = null
        AppPreferences.reminderTime = null
    }

    /**
     * Set currency
     *
     * @param currencyWithSymbol currency with its symbol
     */
    fun setCurrency(currencyWithSymbol: String) {
        for (currency in Constants.CURRENCIES) {
            if (currencyWithSymbol.contains(currency.key)) {
                AppPreferences.currency = currency.key
                _currency.value = currency.key
                return
            }
        }
    }

    /**
     * Set date format
     *
     * @param chosenDateFormat date format
     */
    fun setDateFormat(chosenDateFormat: String) {
        for (dateFormat in Constants.DATE_FORMATS) {
            if (chosenDateFormat.contains(dateFormat)) {
                AppPreferences.dateFormat = dateFormat
                _dateFormat.value = dateFormat
                return
            }
        }
    }

    /**
     * Set daily reminder
     *
     * @param context context
     * @param hourOfDay hour
     * @param minute minute
     */
    fun setDailyReminder(context: Context, hourOfDay: Int, minute: Int) {
        cancelDailyReminder(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Set the alarm to trigger daily at the specified time
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        // Set repeating alarm for every 24 hours
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        setDailyReminderTime(hourOfDay, minute, Locale.getDefault())
    }

    /**
     * Cancel daily reminder
     *
     * @param context context
     */
    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)

        deleteDailyReminderTime()
    }

    /**
     * Delete all accounts
     */
    fun deleteAllAccounts() {
        AppPreferences.jwtToken?.let { token ->
            deleteAllAccounts(token)
        } ?: run {
            _deleteAllAccountsState.value = GetAccountsState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Delete all account with JWT
     *
     * @param token user JWT
     */
    private fun deleteAllAccounts(token: String) {
        _deleteAllAccountsState.value = GetAccountsState(isLoading = true)
        deleteAllAccountsUseCase(token).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _deleteAllAccountsState.value = GetAccountsState()
                }
                is Resource.Error -> {
                    _deleteAllAccountsState.value = GetAccountsState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _deleteAllAccountsState.value = GetAccountsState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}