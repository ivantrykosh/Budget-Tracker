package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction

/**
 * Interface for transaction click listener
 */
interface OnTransactionClickListener {
    fun onTransactionClick(transaction: Transaction)
}