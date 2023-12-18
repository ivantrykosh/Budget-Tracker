package com.ivantrykosh.app.budgettracker.server.domain.repos;

import com.ivantrykosh.app.budgettracker.server.domain.model.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    List<ConfirmationToken> findAllByUserUserIdOrderByConfirmationTokenIdDesc(@NonNull Long userId);

    /**
     * Find confirmation token entity by confirmation token
     * @param confirmationToken confirmation token by which token is found
     * @return Found confirmation token entity
     */
    Optional<ConfirmationToken> findByConfirmationToken(@NonNull String confirmationToken);
}
