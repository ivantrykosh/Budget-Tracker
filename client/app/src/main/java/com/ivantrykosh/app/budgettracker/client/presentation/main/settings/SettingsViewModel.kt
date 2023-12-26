package com.ivantrykosh.app.budgettracker.client.presentation.main.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.use_case.account.delete_all_accounts.DeleteAllAccountsUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.AccountsState
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.DeleteAccountState
import com.ivantrykosh.app.budgettracker.client.presentation.main.settings.notification.DailyReminderReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllAccountsUseCase: DeleteAllAccountsUseCase,
) : ViewModel() {

    private val _deleteAllAccountsState = mutableStateOf(AccountsState())
    val deleteAllAccountsState: State<AccountsState> = _deleteAllAccountsState

    private val _isLoadingDeleteAllAccounts = MutableLiveData<Boolean>(false)
    val isLoadingDeleteAllAccounts: LiveData<Boolean> = _isLoadingDeleteAllAccounts

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
        it ?: "Set time"
    }

    fun getCurrencies(): List<String> {
        return Constants.CURRENCIES.map {
            it.key + " - " + it.value
        }
    }

    fun getDateFormats(): List<String> {
        return Constants.DATE_FORMATS.toList()
    }

    private fun setDailyReminderTime(hours: Int, minutes: Int, locale: Locale) {
//        val calendar = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, hours)
//            set(Calendar.MINUTE, minutes)
//        }
//
//        val pattern = DateFormat.getBestDateTimePattern(locale, "Hm")
//        val simpleDateFormat = SimpleDateFormat(pattern, locale)
//
//        val time = simpleDateFormat.format(calendar.time)
//
//        AppPreferences.reminderTime = time
//        _dailyReminderTime.value = time

        val time = LocalTime.of(hours, minutes)
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)

        val formattedTime = time.format(formatter)

        AppPreferences.reminderTime = formattedTime
        _dailyReminderTime.value = formattedTime
    }

    private fun deleteDailyReminderTime() {
        _dailyReminderTime.value = null
        AppPreferences.reminderTime = null
    }

    private fun getCurrencySymbol(currency: String): String {
        return when (currency) {
            "UAH" -> "₴"
            else -> Currency.getInstance(currency).symbol
        }
    }

    fun setCurrency(currencyWithSymbol: String) {
        for (currency in Constants.CURRENCIES) {
            if (currencyWithSymbol.contains(currency.key)) {
                AppPreferences.currency = currency.key
                _currency.value = currency.key
                return
            }
        }
    }

    fun setDateFormat(chosenDateFormat: String) {
        for (dateFormat in Constants.DATE_FORMATS) {
            if (chosenDateFormat.contains(dateFormat)) {
                AppPreferences.dateFormat = dateFormat
                _dateFormat.value = dateFormat
                return
            }
        }
    }

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

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)

        deleteDailyReminderTime()
    }

    fun deleteAllAccounts() {
        AppPreferences.jwtToken?.let { token ->
            deleteAllAccounts(token)
        } ?: run {
            _deleteAllAccountsState.value = AccountsState(error = "No JWT token found")
        }
    }

    private fun deleteAllAccounts(token: String) {
        _isLoadingDeleteAllAccounts.value = true
        deleteAllAccountsUseCase(token).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _deleteAllAccountsState.value = AccountsState()
                    _isLoadingDeleteAllAccounts.value = false
                }
                is Resource.Error -> {
                    _deleteAllAccountsState.value = AccountsState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingDeleteAllAccounts.value = false
                }
                is Resource.Loading -> {
                    _deleteAllAccountsState.value = AccountsState(isLoading = true)
                    _isLoadingDeleteAllAccounts.value = true
                }
            }
        }.launchIn(viewModelScope)
    }
}