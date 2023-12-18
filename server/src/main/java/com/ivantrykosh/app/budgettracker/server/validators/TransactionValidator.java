package com.ivantrykosh.app.budgettracker.server.validators;

import com.ivantrykosh.app.budgettracker.server.application.services.AccountService;
import com.ivantrykosh.app.budgettracker.server.application.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Utility class for validating transaction-related information.
 */
@Component
public class TransactionValidator {
    @Autowired
    private AccountService accountService;
    @Autowired
    private TransactionService transactionService;

    /**
     * Checks whether the given transactionId is valid.
     *
     * @param transactionId The transactionId to validate.
     * @return True if the transactionId is valid, false otherwise.
     */
    public boolean checkTransactionId(Long transactionId) {
        if (transactionId == null) {
            return false;
        }
        return transactionService.getTransactionById(transactionId) != null;
    }

    /**
     * Checks whether the given accountId is valid.
     *
     * @param accountId The accountId to validate.
     * @return True if the accountId is valid, false otherwise.
     */
    public boolean checkAccountId(Long accountId) {
        if (accountId == null) {
            return false;
        }
        return accountService.getAccountById(accountId) != null;
    }

    /**
     * Checks whether the given category is valid.
     *
     * @param category The category to validate.
     * @return True if the category is valid, false otherwise.
     */
    public boolean checkCategory(String category) {
        if (category == null || category.isBlank()) {
            return false;
        }
        return category.length() <= 50;
    }

    /**
     * Checks whether the given value is valid.
     *
     * @param value The value to validate.
     * @return True if the value is valid, false otherwise.
     */
    public boolean checkValue(Double value) {
        return value != null;
    }

    /**
     * Checks whether the given date is valid.
     *
     * @param date The date to validate.
     * @return True if the date is valid, false otherwise.
     */
    public boolean checkDate(Date date) {
        return date != null;
    }

    /**
     * Checks whether the given toFromWhom is valid.
     *
     * @param toFromWhom The toFromWhom to validate.
     * @return True if the toFromWhom is valid, false otherwise.
     */
    public boolean checkToFromWhom(String toFromWhom) {
        if (toFromWhom == null) {
            return true;
        }
        return !toFromWhom.isBlank() && toFromWhom.length() <= 25;
    }

    /**
     * Checks whether the given note is valid.
     *
     * @param note The note to validate.
     * @return True if the note is valid, false otherwise.
     */
    public boolean checkNote(String note) {
        if (note == null) {
            return true;
        }
        return !note.isBlank() && note.length() <= 100;
    }
}
