package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.model.Transaction;
import com.ivantrykosh.app.budgettracker.server.repos.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Transaction entities.
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Saves a transaction to the database.
     *
     * @param transaction The transaction to be saved.
     * @return The saved transaction.
     */
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param transactionId The ID of the transaction to retrieve.
     * @return The transaction if found, otherwise null.
     */
    public Transaction getTransactionById(Long transactionId) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        return transaction.orElse(null);
    }

    /**
     * Retrieves list of transactions by accountId.
     *
     * @param accountId The ID of the account to retrieve transactions.
     * @return The list of transactions.
     */
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findAllByAccountAccountIdOrderByDateDesc(accountId);
    }

    /**
     * Retrieves list of transactions by accountIds.
     *
     * @param accountIds The IDs of the accounts to retrieve transactions.
     * @return The list of transactions.
     */
    public List<Transaction> getTransactionsByAccountIds(List<Long> accountIds) {
        return transactionRepository.findAllByAccountAccountIdInOrderByDateDesc(accountIds);
    }

    /**
     * Retrieves list of transactions by accountIds and month.
     *
     * @param accountIds The IDs of the accounts to retrieve transactions.
     * @param month The month to retrieve transactions.
     * @return The list of transactions.
     */
    public List<Transaction> getTransactionsByAccountIdsAndMonth(List<Long> accountIds, Date month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        return transactionRepository.findAllByAccountAccountIdInAndDateBetweenOrderByDateDesc(accountIds, startDate, endDate);
    }

    /**
     * Retrieves list of income transactions by accountIds.
     *
     * @param accountIds The IDs of the accounts to retrieve transactions.
     * @param pageNumber The number of page.
     * @param numberOfTransaction The number of transactions to retrieve.
     * @return The list of transactions.
     */
    public List<Transaction> getIncomeTransactionsByAccountIds(List<Long> accountIds, Integer pageNumber, Integer numberOfTransaction) {
        PageRequest pageRequest = PageRequest.of(pageNumber, numberOfTransaction);
        return transactionRepository.findAllByAccountAccountIdInAndValueGreaterThanOrderByDateDesc(accountIds, 0.0, pageRequest);
    }

    /**
     * Retrieves list of expense transactions by accountIds.
     *
     * @param accountIds The IDs of the accounts to retrieve transactions.
     * @param pageNumber The number of page.
     * @param numberOfTransaction The number of transactions to retrieve.
     * @return The list of transactions.
     */
    public List<Transaction> getExpenseTransactionsByAccountIds(List<Long> accountIds, Integer pageNumber, Integer numberOfTransaction) {
        PageRequest pageRequest = PageRequest.of(pageNumber, numberOfTransaction);
        return transactionRepository.findAllByAccountAccountIdInAndValueLessThanOrderByDateDesc(accountIds, 0.0, pageRequest);
    }

    /**
     * Calculate sum of transactions with specified type and accountId.
     *
     * @param accountId The ID of the account to retrieve transaction.
     * @param isIncome The type of transaction. If income, then isIncome has to be true, otherwise false.
     * @return The sum of transactions
     */
    public Double getSumOfTransactionsWithAccountIdAndSpecifiedType(Long accountId, boolean isIncome) {
        return transactionRepository.calculateSumByAccountIdAndType(accountId, isIncome);
    }

    /**
     * Updates a transaction in the database.
     *
     * @param transaction The transaction to be updated.
     * @return The updated transaction.
     */
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    /**
     * Deletes a transaction by their ID.
     *
     * @param transactionId The ID of the transaction to delete.
     * @return The deleted transaction if found, otherwise null.
     */
    public Transaction deleteTransactionById(Long transactionId) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        transactionRepository.deleteById(transactionId);
        return transaction.orElse(null);
    }
}
