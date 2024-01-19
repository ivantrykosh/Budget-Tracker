package com.ivantrykosh.app.budgettracker.client.common

import androidx.room.TypeConverter
import java.util.Date

/**
 * Class for converter functions
 */
class Converters {

    /**
     * Convert value from Long to Date
     *
     * @param value date
     */
    @TypeConverter
    fun longToDate(value: Long?): Date? {
        return value?.let {
            Date(value)
        }
    }

    /**
     * Convert value from Date to Long
     *
     * @param date date to convert
     */
    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.time
    }
}