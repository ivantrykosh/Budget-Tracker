package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import com.ivantrykosh.app.budgettracker.client.domain.model.Account

/**
 * Interface for account click listener
 */
interface OnAccountClickListener {
    fun onAccountClick(account: Account)
}