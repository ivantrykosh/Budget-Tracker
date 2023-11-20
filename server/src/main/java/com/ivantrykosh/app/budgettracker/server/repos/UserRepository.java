package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing User entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
