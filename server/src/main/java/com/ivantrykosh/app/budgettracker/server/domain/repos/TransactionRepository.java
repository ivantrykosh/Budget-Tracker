package com.ivantrykosh.app.budgettracker.server.domain.repos;

import com.ivantrykosh.app.budgettracker.server.domain.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository interface for managing Transaction entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transaction by account ID
     * @param accountId accountId by which transactions are found
     * @return Found transactions
     */
    List<Transaction> findAllByAccountAccountIdOrderByDateDescTransactionIdDesc(@NonNull Long accountId);

    /**
     * Find all transaction by accounts IDs
     * @param accountIds accountIds by which transactions are found
     * @return Found transactions
     */
    List<Transaction> findAllByAccountAccountIdInOrderByDateDescTransactionIdDesc(@NonNull List<Long> accountIds);

    /**
     * Find all transaction by account IDs and between start date and end date
     * @param accountIds accountIds by which transactions are found
     * @param startDate startDate by which transaction are found
     * @param endDate endDate by which transaction are found
     * @return Found transactions
     */
    List<Transaction> findAllByAccountAccountIdInAndDateBetweenOrderByDateDescTransactionIdDesc(@NonNull List<Long> accountIds, @NonNull Date startDate, @NonNull Date endDate);

    /**
     * Finds all transactions with pageable size for a list of account IDs where the transaction value is greater than a specified threshold.
     * Results are ordered by date in descending order
     *
     * @param accountIds list of account IDs to retrieve transactions for
     * @param value the minimum transaction value required
     * @param pageable pagination information, specifying the page number and size
     * @return A list of transactions matching the criteria
     */
    List<Transaction> findAllByAccountAccountIdInAndValueGreaterThanOrderByDateDescTransactionIdDesc(@NonNull List<Long> accountIds, @NonNull Double value, Pageable pageable);

    /**
     * Finds all transactions with pageable size for a list of account IDs where the transaction value is less than a specified threshold.
     * Results are ordered by date in descending order
     *
     * @param accountIds list of account IDs to retrieve transactions for
     * @param value the maximum transaction value required
     * @param pageable pagination information, specifying the page number and size
     * @return A list of transactions matching the criteria
     */
    List<Transaction> findAllByAccountAccountIdInAndValueLessThanOrderByDateDescTransactionIdDesc(@NonNull List<Long> accountIds, @NonNull Double value, Pageable pageable);

    /**
     * Calculates the sum of either incomes or expenses for a specified account ID.
     *
     * @param accountId The ID of the account for which to calculate the sum.
     * @param isIncome If true, calculates the sum of incomes; if false, calculates the sum of expenses.
     * @return The calculated sum.
     */
    @Query("SELECT " +
            "SUM(CASE WHEN :isIncome = true THEN CASE WHEN t.value > 0 THEN t.value ELSE 0 END " +
            "ELSE CASE WHEN t.value < 0 THEN t.value ELSE 0 END END) AS sumResult " +
            "FROM Transaction t WHERE t.account.id = :accountId")
    Double calculateSumByAccountIdAndType(@Param("accountId") Long accountId, @Param("isIncome") boolean isIncome);
}
