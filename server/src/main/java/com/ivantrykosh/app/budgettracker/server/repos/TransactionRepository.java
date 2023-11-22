package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<Transaction> findAllByAccountAccountId(@NonNull Long accountId);

    /**
     * Find all transaction by accounts IDs
     * @param accountIds accountIds by which transactions are found
     * @return Found transactions
     */
    List<Transaction> findAllByAccountAccountIdIn(@NonNull List<Long> accountIds);

    /**
     * Find all transaction by account IDs and between start date and end date
     * @param accountIds accountIds by which transactions are found
     * @param startDate startDate by which transaction are found
     * @param endDate endDate by which transaction are found
     * @return Found transactions
     */
    List<Transaction> findAllByAccountAccountIdInAndDateBetween(@NonNull List<Long> accountIds, @NonNull Date startDate, @NonNull Date endDate);
}
