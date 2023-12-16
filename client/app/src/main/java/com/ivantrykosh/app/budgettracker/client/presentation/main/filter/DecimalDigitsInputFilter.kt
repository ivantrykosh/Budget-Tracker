package com.ivantrykosh.app.budgettracker.client.presentation.main.filter

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class DecimalDigitsInputFilter(
    private val digitsBeforePoint: Int,
    private val digitsAfterPoint: Int,
) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val pattern = Pattern.compile("[0-9]{0," + (digitsBeforePoint-1) + "}+((\\.[0-9]{0," + (digitsAfterPoint-1) + "})?)||(\\.)?")
        val matcher = pattern.matcher(dest)
        if (!matcher.matches()) {
            return ""
        }
        return null
    }

}