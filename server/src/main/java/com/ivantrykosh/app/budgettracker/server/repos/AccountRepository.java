package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Account entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
