package com.ivantrykosh.app.budgettracker.server.repos;

import com.ivantrykosh.app.budgettracker.server.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Category entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
