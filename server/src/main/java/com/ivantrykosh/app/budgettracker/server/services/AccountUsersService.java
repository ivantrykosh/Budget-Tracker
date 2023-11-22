package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.repos.AccountUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class for managing AccountUsers entities.
 */
@Service
public class AccountUsersService {

    @Autowired
    private AccountUsersRepository accountUsersRepository;

    /**
     * Saves account users to the database.
     *
     * @param accountUsers The account users to be saved.
     * @return The saved account users.
     */
    public AccountUsers saveAccountUsers(AccountUsers accountUsers) {
        return accountUsersRepository.save(accountUsers);
    }

    /**
     * Retrieves account users by their ID.
     *
     * @param accountUsersId The ID of the account users to retrieve.
     * @return The account users if found, otherwise null.
     */
    public AccountUsers getAccountUsersById(Long accountUsersId) {
        Optional<AccountUsers> accountUsers = accountUsersRepository.findById(accountUsersId);
        return accountUsers.orElse(null);
    }

    /**
     * Retrieves all accounts users by userId
     * @param userId The ID of the user to retrieve
     * @return The list of account users
     */
    public List<AccountUsers> getAccountsUsersByUserId(Long userId) {
        return Stream.of(
                        accountUsersRepository.findAllByUser2Id(userId),
                        accountUsersRepository.findAllByUser3Id(userId),
                        accountUsersRepository.findAllByUser4Id(userId)
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Updates account users in the database.
     *
     * @param accountUsers The account users to be updated.
     * @return The updated account users.
     */
    public AccountUsers updateUser(AccountUsers accountUsers) {
        return accountUsersRepository.save(accountUsers);
    }

    /**
     * Deletes account users by their ID.
     *
     * @param accountUsersId The ID of the account users to delete.
     * @return The deleted account users if found, otherwise null.
     */
    public AccountUsers deleteUserById(Long accountUsersId) {
        Optional<AccountUsers> accountUsers = accountUsersRepository.findById(accountUsersId);
        accountUsersRepository.deleteById(accountUsersId);
        return accountUsers.orElse(null);
    }
}
