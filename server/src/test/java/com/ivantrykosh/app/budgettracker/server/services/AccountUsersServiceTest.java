package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.model.User;
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
 * Test AccountUsersService
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({AccountUsersService.class, AccountService.class, UserService.class})
class AccountUsersServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountUsersService accountUsersService;

    private User user1;
    private User user2;
    private Account account;

    /**
     * Save users and account to db
     */
    @BeforeEach
    public void saveUsersAndAccount() {
        // First user
        User newUser = new User();
        newUser.setEmail("testemail@gmail.com");
        newUser.setPasswordHash("hash");
        newUser.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        newUser.setIsVerified(false);
        user1 = userService.saveUser(newUser);

        // Second user
        User newUser2 = new User();
        newUser2.setEmail("testemail2@gmail.com");
        newUser2.setPasswordHash("hash");
        newUser2.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        newUser2.setIsVerified(false);
        user2 = userService.saveUser(newUser2);

        // Account
        Account newAccount = new Account();
        newAccount.setName("accountTest");
        newAccount.setUser(user1);
        account = accountService.saveAccount(newAccount);
    }

    /**
     * Test saving AccountUsers
     */
    @Test
    void saveAccountUsers() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        // Print account users and saved account users
        System.out.println(accountUsers + "\n" + savedAccountUsers);

        // Assert parameters are equals
        assertEquals(accountUsers.getAccount().getAccountId(), savedAccountUsers.getAccount().getAccountId(), "Accounts are not equals!");
    }

    /**
     * Test saving AccountUsers with invalid data
     */
    @Test
    void saveAccountUsersWihInvalidData() {
        AccountUsers accountUsers = createNewValidAccountUsers();
        accountUsers.setAccount(null);

        System.out.println(accountUsers);

        assertThrows(Exception.class, () -> accountUsersService.saveAccountUsers(accountUsers), "Exception was not thrown!");
    }

    /**
     * Test getting AccountUsers by ID
     */
    @Test
    void getAccountUsersById() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        // Get account users by ID
        AccountUsers retrievedAccountUsers = accountUsersService.getAccountUsersById(savedAccountUsers.getAccountUsersId());

        // Print saved and retrieved account users
        System.out.println(savedAccountUsers + "\n" + retrievedAccountUsers);

        // Assert parameters are equals
        assertEquals(savedAccountUsers.getUser2Id(), retrievedAccountUsers.getUser2Id(), "User2IDs are not equals!");
    }

    /**
     * Test getting AccountUsers with invalid ID
     */
    @Test
    void getAccountUsersByInvalidId() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        assertNull(accountUsersService.getAccountUsersById(Long.MAX_VALUE), "Account users are not empty!");
    }

    /**
     * Test getting AccountUsers by Account ID
     */
    @Test
    void getAccountUsersByAccountId() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        // Get account users by ID
        AccountUsers retrievedAccountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());

        // Print saved and retrieved account users
        System.out.println(savedAccountUsers + "\n" + retrievedAccountUsers);

        // Assert parameters are equals
        assertEquals(savedAccountUsers.getUser2Id(), retrievedAccountUsers.getUser2Id(), "User2IDs are not equals!");
    }

    /**
     * Test getting AccountUsers with invalid account ID
     */
    @Test
    void getAccountUsersByInvalidAccountId() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        assertNull(accountUsersService.getAccountUsersByAccountId(Long.MAX_VALUE), "Account users are not empty!");
    }

    /**
     * Test getting AccountUsers by user ID
     */
    @Test
    void getAccountsUsersByUserId() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        // Get account users by user ID
        List<AccountUsers> retrievedAccountUsers = accountUsersService.getAccountsUsersByUserId(user2.getUserId());

        // Print saved and retrieved account users
        System.out.println(savedAccountUsers + "\n" + retrievedAccountUsers);

        // Assert parameters are equals
        assertEquals(1, retrievedAccountUsers.size(), "Size of list is not 1!");
        assertEquals(savedAccountUsers.getAccount().getAccountId(), retrievedAccountUsers.get(0).getAccount().getAccountId(), "Account IDs are not equals!");
    }

    /**
     * Test getting AccountUsers by invalid user ID
     */
    @Test
    void getAccountsUsersByInvalidUserId() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        assertEquals(0, accountUsersService.getAccountsUsersByUserId(Long.MAX_VALUE).size(), "List is not empty!");
    }

    /**
     * Test updating AccountUsers
     */
    @Test
    void updateAccountUsers() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        // Update user3Id
        accountUsers.setUser3Id(2L);

        // Update account users
        AccountUsers updatedAccountUsers = accountUsersService.updateAccountUsers(accountUsers);

        // Print account and updatedAccount
        System.out.println(savedAccountUsers + "\n" + updatedAccountUsers);

        // Assert parameters are equals
        assertEquals(savedAccountUsers.getAccountUsersId(), updatedAccountUsers.getAccountUsersId(), "IDs are not equals!");
        assertEquals(savedAccountUsers.getUser2Id(), updatedAccountUsers.getUser2Id(), "User2Ids are not equals!");
    }

    /**
     * Test deleting AccountUsers
     */
    @Test
    void deleteAccountUsersById() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        // Delete account users
        AccountUsers deletedAccountUsers = accountUsersService.deleteAccountUsersById(savedAccountUsers.getAccountUsersId());

        // Print account and deleted account
        System.out.println(savedAccountUsers + "\n" + deletedAccountUsers);

        // Assert parameters are equals
        assertEquals(savedAccountUsers.getUser2Id(), deletedAccountUsers.getUser2Id(), "User2Ids are not equals!");
        assertNull(accountUsersService.getAccountUsersById(savedAccountUsers.getAccountUsersId()), "Account users is not deleted!");
    }

    /**
     * Test deleting not existing AccountUsers
     */
    @Test
    void deleteNotExistingAccountUsersById() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        assertNull(accountUsersService.getAccountUsersById(Long.MAX_VALUE), "Account users is deleted!");
    }

    /**
     * Test deleting user IDs in AccountUsers
     */
    @Test
    void deleteUserIdFromAccountUsers() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        // Delete user IDs from account users
        List<AccountUsers> retrievedAccountUsers = accountUsersService.deleteUserIdFromAccountUsers(user2.getUserId());

        // Print saved and retrieved account users
        System.out.println(savedAccountUsers + "\n" + retrievedAccountUsers);

        // Assert parameters are equals
        assertNull(retrievedAccountUsers.get(0).getUser2Id(), "User2Id is not null!");
        assertEquals(0, accountUsersService.getAccountsUsersByUserId(user2.getUserId()).size(), "User IDs are not deleted!");
    }

    /**
     * Test deleting invalid user IDs in AccountUsers
     */
    @Test
    void deleteInvalidUserIdFromAccountUsers() {
        AccountUsers accountUsers = createNewValidAccountUsers();

        // Save account users
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        assertEquals(0, accountUsersService.deleteUserIdFromAccountUsers(Long.MAX_VALUE).size(), "List is not empty!");
    }

    /**
     * Create new valid AccountUsers
     * @return new valid AccountUsers
     */
    private AccountUsers createNewValidAccountUsers() {
        AccountUsers newAccountUsers = new AccountUsers();
        newAccountUsers.setUser2Id(user2.getUserId());
        newAccountUsers.setAccount(account);

        return newAccountUsers;
    }
}