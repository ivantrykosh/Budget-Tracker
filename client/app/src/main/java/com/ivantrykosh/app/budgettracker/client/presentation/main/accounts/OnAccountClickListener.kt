package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import com.ivantrykosh.app.budgettracker.client.domain.model.Account

/**
 * Interface for account click listener
 */
interface OnAccountClickListener {
    /**
     * On account click method
     *
     * @param account clicked account
     */
    fun onAccountClick(account: Account)
}