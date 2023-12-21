package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import com.ivantrykosh.app.budgettracker.client.domain.model.Account

interface OnAccountClickListener {
    fun onAccountClick(account: Account)
}