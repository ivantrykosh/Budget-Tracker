package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Account entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts by user ID
     * @param userId userId by which accounts are found
     * @return Found accounts
     */
    List<Account> findAllByUserUserId(@NonNull Long userId);
}
