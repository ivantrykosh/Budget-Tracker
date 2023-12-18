package com.ivantrykosh.app.budgettracker.server.domain.repos;

import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing User entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     * @param email email by which user is found
     * @return found user
     */
    Optional<User> findByEmail(@NonNull String email);
}
