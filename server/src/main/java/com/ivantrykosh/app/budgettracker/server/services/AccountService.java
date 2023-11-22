package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.repos.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Account entities.
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Saves an account to the database.
     *
     * @param account The account to be saved.
     * @return The saved account.
     */
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Retrieves an account by its ID.
     *
     * @param accountId The ID of the account to retrieve.
     * @return The account if found, otherwise null.
     */
    public Account getAccountById(Long accountId) {
        Optional<Account> account = accountRepository.findById(accountId);
        return account.orElse(null);
    }

    /**
     * Retrieves list of accounts by their userId.
     *
     * @param userId The ID of the account owner user to retrieve.
     * @return The list of accounts.
     */
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findAllByUserUserId(userId);
    }

    /**
     * Updates an account in the database.
     *
     * @param account The account to be updated.
     * @return The updated account.
     */
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Deletes an account by their ID.
     *
     * @param accountId The ID of the account to delete.
     * @return The deleted account if found, otherwise null.
     */
    public Account deleteAccountById(Long accountId) {
        Optional<Account> account = accountRepository.findById(accountId);
        accountRepository.deleteById(accountId);
        return account.orElse(null);
    }
}
