package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.application.services.AccountService;
import com.ivantrykosh.app.budgettracker.server.application.services.UserService;
import com.ivantrykosh.app.budgettracker.server.domain.model.Account;
import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test AccountService
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({AccountService.class, UserService.class})
class AccountServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    private User user;

    /**
     * Save user to db
     */
    @BeforeEach
    public void saveUser() {
        User newUser = new User();
        newUser.setEmail("testemail@gmail.com");
        newUser.setPasswordHash("hash");
        newUser.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        newUser.setIsVerified(false);
        user = userService.saveUser(newUser);
    }

    /**
     * Test saving Account
     */
    @Test
    void saveAccount() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        // Print account and saved account
        System.out.println(account + "\n" + savedAccount);

        // Assert parameters are equals
        assertEquals(account.getName(), savedAccount.getName(), "Names are not equals!");
    }

    /**
     * Test saving Account with invalid data
     */
    @Test
    void saveAccountWihInvalidData() {
        Account account = createNewValidAccount();
        account.setName(null);

        System.out.println(account);

        assertThrows(Exception.class, () -> accountService.saveAccount(account), "Exception was not thrown!");
    }

    /**
     * Test getting Account
     */
    @Test
    void getAccountById() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        // Get account by ID
        Account retrievedAccount = accountService.getAccountById(savedAccount.getAccountId());

        // Print account, saved account and retrieved account
        System.out.println(account + "\n" + savedAccount + "\n" + retrievedAccount);

        // Assert parameters are equals
        assertEquals(savedAccount.getName(), retrievedAccount.getName(), "Names are not equals!");
    }

    /**
     * Test getting Account with invalid ID
     */
    @Test
    void getAccountByInvalidId() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        assertNull(accountService.getAccountById(Long.MAX_VALUE), "Account is not null!");
    }

    /**
     * Test getting Accounts with user ID
     */
    @Test
    void getAccountsByUserId() {
        Account account1 = createNewValidAccount();

        Account account2 = createNewValidAccount();
        account2.setName("test account 2");

        // Save accounts
        Account savedAccount1 = accountService.saveAccount(account1);
        Account savedAccount2 = accountService.saveAccount(account2);

        // Get accounts
        List<Account> retrievedAccounts = accountService.getAccountsByUserId(user.getUserId());

        // Print saved and retrieved accounts
        System.out.println(savedAccount1 + "\n" + savedAccount2 + "\n" + retrievedAccounts);

        // Assert parameters are equals
        assertEquals(2, retrievedAccounts.size(), "Size of account's list is not 2!");
        assertEquals(savedAccount1.getName(), retrievedAccounts.get(0).getName(), "Names are not equals!");
        assertEquals(savedAccount2.getName(), retrievedAccounts.get(1).getName(), "Names are not equals!");
    }

    /**
     * Test getting Account with invalid user ID
     */
    @Test
    void getAccountsByInvalidUserId() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        assertEquals(0, accountService.getAccountsByUserId(Long.MAX_VALUE).size(), "List is not empty!");
    }

    /**
     * Test updating Account
     */
    @Test
    void updateAccount() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        // Update name
        savedAccount.setName("new test name");

        // Update account
        Account updatedAccount = accountService.updateAccount(savedAccount);

        // Print account and updatedAccount
        System.out.println(savedAccount + "\n" + updatedAccount);

        // Assert parameters are equals
        assertEquals(savedAccount.getAccountId(), updatedAccount.getAccountId(), "IDs are not equals!");
        assertEquals(savedAccount.getName(), updatedAccount.getName(), "Names are not equals!");
    }

    /**
     * Test deleting Account
     */
    @Test
    void deleteAccountById() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        // Delete account
        Account deletedAccount = accountService.deleteAccountById(savedAccount.getAccountId());

        // Print account and deleted account
        System.out.println(savedAccount + "\n" + deletedAccount);

        // Assert parameters are equals
        assertEquals(savedAccount.getName(), deletedAccount.getName(), "Names are not equals!");
        assertNull(accountService.getAccountById(savedAccount.getAccountId()), "Account is not deleted!");
    }

    /**
     * Test deleting not existing Account
     */
    @Test
    void deleteNotExistingConfirmationTokenById() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        assertNull(accountService.getAccountById(Long.MAX_VALUE), "Account is deleted!");
    }

    /**
     * Test deleting Accounts with user ID
     */
    @Test
    void deleteAccountsByUserId() {
        Account account1 = createNewValidAccount();

        Account account2 = createNewValidAccount();
        account2.setName("test account 2");

        // Save accounts
        Account savedAccount1 = accountService.saveAccount(account1);
        Account savedAccount2 = accountService.saveAccount(account2);

        // Delete accounts
        List<Account> retrievedAccounts = accountService.deleteAccountsByUserId(user.getUserId());

        // Print saved and retrieved accounts
        System.out.println(savedAccount1 + "\n" + savedAccount2 + "\n" + retrievedAccounts);

        // Assert parameters are equals
        assertEquals(2, retrievedAccounts.size(), "Size of account's list is not 2!");
        assertEquals(savedAccount1.getName(), retrievedAccounts.get(0).getName(), "Names are not equals!");
        assertEquals(savedAccount2.getName(), retrievedAccounts.get(1).getName(), "Names are not equals!");
        assertEquals(0, accountService.getAccountsByUserId(user.getUserId()).size(), "Accounts are not deleted!");
    }

    /**
     * Test deleting Account with invalid user ID
     */
    @Test
    void deleteAccountsByInvalidUserId() {
        Account account = createNewValidAccount();

        // Save account
        Account savedAccount = accountService.saveAccount(account);

        assertEquals(0, accountService.deleteAccountsByUserId(Long.MAX_VALUE).size(), "List is not empty!");
    }

    /**
     * Create new valid Account
     * @return new valid Account
     */
    private Account createNewValidAccount() {
        // Account
        Account account = new Account();
        account.setName("test account");
        account.setUser(user);

        return account;
    }

}