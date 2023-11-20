package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Transaction entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
