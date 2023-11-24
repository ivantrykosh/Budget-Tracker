package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Confirmation Token entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    /**
     * Find all confirmation tokens by user ID
     * @param userId userId by which tokens are found
     * @return Found confirmation tokens
     */
    List<ConfirmationToken> findAllByUserUserId(@NonNull Long userId);
}
