package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Confirmation Token entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
}
