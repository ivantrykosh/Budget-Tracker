package com.ivantrykosh.app.budgettracker.server.controllers;

import com.ivantrykosh.app.budgettracker.server.dtos.UserDto;
import com.ivantrykosh.app.budgettracker.server.mappers.Mapper;
import com.ivantrykosh.app.budgettracker.server.mappers.UserMapper;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.requests.ChangePasswordRequest;
import com.ivantrykosh.app.budgettracker.server.services.*;
import com.ivantrykosh.app.budgettracker.server.util.PasswordGenerator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User REST controller
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ConfirmationTokenService confirmationTokenService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountUsersService accountUsersService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final Mapper<User, UserDto> mapper = new UserMapper();

    /**
     * Endpoint to retrieve information about the currently authenticated user.
     *
     * @return ResponseEntity containing the UserDto with user information and HttpStatus indicating the result.
     */
    @GetMapping("/get")
    public ResponseEntity<UserDto> getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        UserDto userDto = mapper.convertToDto(user);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    /**
     * Endpoint to delete the currently authenticated user and their all data, including family accounts,
     * where the current user is the owner.
     *
     * @return ResponseEntity with a success message and HttpStatus indicating the result.
     */
    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<String> deleteUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        confirmationTokenService.deleteConfirmationTokensByUserId(user.getUserId());

        List<Account> accounts = accountService.getAccountsByUserId(user.getUserId());
        for (Account account : accounts) {
            transactionService.deleteTransactionsByAccountId(account.getAccountId());

            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            accountUsersService.deleteAccountUsersById(accountUsers.getAccountUsersId());
        }

        accountUsersService.deleteUserIdFromAccountUsers(user.getUserId());

        accountService.deleteAccountsByUserId(user.getUserId());

        userService.deleteUserById(user.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body("User is deleted!");
    }

    @PatchMapping("/change-password")
    public ResponseEntity<String> changeUserPassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(changePasswordRequest.getNewPassword())
        );
        userService.updateUser(user);
        return ResponseEntity.status(HttpStatus.OK).body("Password is changed!");
    }

    /**
     * Endpoint to reset the password for the currently authenticated user.
     *
     * @return ResponseEntity with a success message containing the new password and HttpStatus indicating the result.
     */
    @PatchMapping("/reset-password")
    public ResponseEntity<String> resetUserPassword() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        String generatedPassword = new PasswordGenerator()
                .generatePassword(10);
        user.setPasswordHash(
                passwordEncoder.encode(generatedPassword)
        );
        userService.updateUser(user);

        // todo send email with new password instead of return one in response body

        return ResponseEntity.status(HttpStatus.OK).body("Password is changed! New password: " + generatedPassword);
    }
}
