package com.ivantrykosh.app.budgettracker.server.domain.repos;

import com.ivantrykosh.app.budgettracker.server.domain.model.AccountUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Account entities.
 * Extends JpaRepository, providing CRUD and pagination functionality.
 */
@Repository
public interface AccountUsersRepository extends JpaRepository<AccountUsers, Long> {

    /**
     * Find all account users by account ID
     * @param accountId accountId by which account users are found
     * @return Found account users
     */
    Optional<AccountUsers> findByAccountAccountId(@NonNull Long accountId);

    /**
     * Find all accounts users by user2Id
     * @param user2Id user2Id by which accounts users are found
     * @return Found accounts users
     */
    List<AccountUsers> findAllByUser2Id(@NonNull Long user2Id);

    /**
     * Find all accounts users by user3Id
     * @param user3Id user3Id by which accounts users are found
     * @return Found accounts users
     */
    List<AccountUsers> findAllByUser3Id(@NonNull Long user3Id);

    /**
     * Find all accounts users by user4Id
     * @param user4Id user4Id by which accounts users are found
     * @return Found accounts users
     */
    List<AccountUsers> findAllByUser4Id(@NonNull Long user4Id);
}
