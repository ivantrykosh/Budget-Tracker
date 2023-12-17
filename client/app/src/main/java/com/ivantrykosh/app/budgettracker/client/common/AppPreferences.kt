package com.ivantrykosh.app.budgettracker.client.common

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.Currency
import java.util.Locale

object AppPreferences {
    private var sharedPreferences: SharedPreferences? = null

    fun setup(context: Context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE)

        // Setup default values for currency and date format, if it doesn't present
        currency = currency ?: Constants.CURRENCIES.elementAt(0)
        dateFormat = dateFormat ?: Constants.DATE_FORMATS.elementAt(0)
    }

    var jwtToken: String?
        get() = Key.JWT_TOKEN.getString()
        set(value) = Key.JWT_TOKEN.setString(value)

    var currency: String?
        get() = Key.CURRENCY.getString()
        set(value) = Key.CURRENCY.setString(value)

    var dateFormat: String?
        get() = Key.DATE_FORMAT.getString()
        set(value) = Key.DATE_FORMAT.setString(value)

    private enum class Key {
        JWT_TOKEN, CURRENCY, DATE_FORMAT;

        fun getBoolean(): Boolean? = if (sharedPreferences!!.contains(name)) sharedPreferences!!.getBoolean(name, false) else null
        fun getFloat(): Float? = if (sharedPreferences!!.contains(name)) sharedPreferences!!.getFloat(name, 0f) else null
        fun getInt(): Int? = if (sharedPreferences!!.contains(name)) sharedPreferences!!.getInt(name, 0) else null
        fun getLong(): Long? = if (sharedPreferences!!.contains(name)) sharedPreferences!!.getLong(name, 0) else null
        fun getString(): String? = if (sharedPreferences!!.contains(name)) sharedPreferences!!.getString(name, "") else null

        fun setBoolean(value: Boolean?) = value?.let { sharedPreferences!!.edit { putBoolean(name, value) } } ?: remove()
        fun setFloat(value: Float?) = value?.let { sharedPreferences!!.edit { putFloat(name, value) } } ?: remove()
        fun setInt(value: Int?) = value?.let { sharedPreferences!!.edit { putInt(name, value) } } ?: remove()
        fun setLong(value: Long?) = value?.let { sharedPreferences!!.edit { putLong(name, value) } } ?: remove()
        fun setString(value: String?) = value?.let { sharedPreferences!!.edit { putString(name, value) } } ?: remove()

        fun remove() = sharedPreferences!!.edit { remove(name) }
    }
}