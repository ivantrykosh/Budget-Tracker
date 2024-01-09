package com.ivantrykosh.app.budgettracker.client.common

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Object for saved and retrieved app preferences
 */
object AppPreferences {
    private var sharedPreferences: SharedPreferences? = null

    /**
     * Set up shared preferences
     */
    fun setup(context: Context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE)

        // Setup default values for currency and date format, if it doesn't present
        currency = currency ?: Constants.CURRENCIES.entries.elementAt(0).key
        dateFormat = dateFormat ?: Constants.DATE_FORMATS.elementAt(0)
    }

    /**
     * JWT preference
     */
    var jwtToken: String?
        get() = Key.JWT_TOKEN.getString()
        set(value) = Key.JWT_TOKEN.setString(value)

    /**
     * Currency preference
     */
    var currency: String?
        get() = Key.CURRENCY.getString()
        set(value) = Key.CURRENCY.setString(value)

    /**
     * Date format preference
     */
    var dateFormat: String?
        get() = Key.DATE_FORMAT.getString()
        set(value) = Key.DATE_FORMAT.setString(value)

    /**
     * Reminder time preference
     */
    var reminderTime: String?
        get() = Key.REMINDER_TIME.getString()
        set(value) = Key.REMINDER_TIME.setString(value)

    /**
     * App preferences
     */
    private enum class Key {
        JWT_TOKEN, CURRENCY, DATE_FORMAT, REMINDER_TIME;

        /**
         * Get string from preferences by key
         */
        fun getString(): String? = if (sharedPreferences!!.contains(name)) sharedPreferences!!.getString(name, "") else null

        /**
         * Set string for preference by key and value or remove it, if value is null
         *
         * @param value value to set. If it's null, then remove preference
         */
        fun setString(value: String?) = value?.let { sharedPreferences!!.edit { putString(name, value) } } ?: remove()

        /**
         * Remove preference by key
         */
        fun remove() = sharedPreferences!!.edit { remove(name) }
    }
}