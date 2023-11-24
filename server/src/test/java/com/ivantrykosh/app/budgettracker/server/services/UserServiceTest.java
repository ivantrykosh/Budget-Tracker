package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test UserService
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(UserService.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    /**
     * Test saving User
     */
    @Test
    void saveUser() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        // Print user and savedUser
        System.out.println(user + "\n" + savedUser);

        // Assert unique parameters are equals
        assertEquals(user.getEmail(), savedUser.getEmail(), "Emails of users are not equal!");
    }

    /**
     * Test saving User with invalid data
     */
    @Test
    void saveUserWithInvalidData() {
        User user = createNewValidUser();
        user.setEmail(null);

        System.out.println(user);

        assertThrows(Exception.class, () -> userService.saveUser(user), "Exception was not thrown!");
    }

    /**
     * Test getting User by ID
     */
    @Test
    void getUserById() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        // Get by ID
        User retrievedUser = userService.getUserById(savedUser.getUserId());

        // Print user, savedUser and retrievedUser
        System.out.println(user + "\n" + savedUser + "\n" + retrievedUser);

        // Assert unique parameters are equals
        assertEquals(savedUser.getEmail(), retrievedUser.getEmail(), "Emails of users are not equal!");
    }

    /**
     * Test getting User with invalid ID
     */
    @Test
    void getUserByInvalidId() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        assertNull(userService.getUserById(Long.MAX_VALUE), "User is not null!");
    }

    /**
     * Test getting User by email
     */
    @Test
    void getUserByEmail() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        // Get by ID
        User retrievedUser = userService.getUserByEmail(savedUser.getEmail());

        // Print user, savedUser and retrievedUser
        System.out.println(user + "\n" + savedUser + "\n" + retrievedUser);

        // Assert unique parameters are equals
        assertEquals(savedUser.getUserId(), retrievedUser.getUserId(), "IDs of users are not equal!");
    }

    /**
     * Test getting User with invalid email
     */
    @Test
    void getUserByInvalidEmail() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        assertNull(userService.getUserByEmail(""), "User is not null!");
    }

    /**
     * Test updating User
     */
    @Test
    void updateUser() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        // Update IsVerified
        savedUser.setIsVerified(true);

        // Update user
        User updatedUser = userService.updateUser(savedUser);

        // Print user and updatedUser
        System.out.println(savedUser + "\n" + updatedUser);

        // Assert parameters are equals
        assertEquals(savedUser.getUserId(), updatedUser.getUserId(), "IDs of users are not equal!");
        assertEquals(savedUser.getEmail(), updatedUser.getEmail(), "Emails of users are not equal!");
        assertEquals(savedUser.getIsVerified(), updatedUser.getIsVerified(), "IsVerified of users are not equal!");
    }

    /**
     * Test deleting User
     */
    @Test
    void deleteUserById() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        // Delete user
        User deletedUser = userService.deleteUserById(savedUser.getUserId());

        // Print savedUser and deletedUser
        System.out.println(savedUser + "\n" + deletedUser);

        // Assert parameters are equals
        assertEquals(savedUser.getEmail(), deletedUser.getEmail(), "Emails of users are not equal!");
        assertNull(userService.getUserById(savedUser.getUserId()), "User is not deleted!");
    }

    /**
     * Test deleting not existing User
     */
    @Test
    void deleteNotExistingUserById() {
        User user = createNewValidUser();

        // Save user
        User savedUser = userService.saveUser(user);

        assertNull(userService.deleteUserById(Long.MAX_VALUE), "User is deleted!");
    }

    /**
     * Create new valid User
     * @return new valid User
     */
    private User createNewValidUser() {
        // User data
        User user = new User();
        user.setEmail("testemail@gmail.com");
        user.setPasswordSalt("salt");
        user.setPasswordHash("hash");
        user.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        user.setIsVerified(false);

        return user;
    }
}