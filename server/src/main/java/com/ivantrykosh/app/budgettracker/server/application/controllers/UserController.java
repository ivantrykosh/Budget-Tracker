package com.ivantrykosh.app.budgettracker.server.application.controllers;

import com.ivantrykosh.app.budgettracker.server.application.dtos.UserDto;
import com.ivantrykosh.app.budgettracker.server.application.services.*;
import com.ivantrykosh.app.budgettracker.server.infrastructure.email.EmailSenderService;
import com.ivantrykosh.app.budgettracker.server.application.mappers.Mapper;
import com.ivantrykosh.app.budgettracker.server.application.mappers.UserMapper;
import com.ivantrykosh.app.budgettracker.server.domain.model.Account;
import com.ivantrykosh.app.budgettracker.server.domain.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import com.ivantrykosh.app.budgettracker.server.presentation.requests.ChangePasswordRequest;
import com.ivantrykosh.app.budgettracker.server.presentation.requests.RegisterAndLoginRequest;
import com.ivantrykosh.app.budgettracker.server.presentation.responses.TokenResponse;
import com.ivantrykosh.app.budgettracker.server.util.CustomUserDetails;
import com.ivantrykosh.app.budgettracker.server.util.PasswordManager;
import com.ivantrykosh.app.budgettracker.server.validators.UserValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    @Autowired
    private EmailSenderService emailSenderService;
    @Autowired
    private AuthenticationManager authenticationManager;
    private final UserValidator userValidator = new UserValidator();
    private final Mapper<User, UserDto> mapper = new UserMapper();
    private final String SUBJECT = "New password"; // Email subject
    Logger logger = LoggerFactory.getLogger(UserController.class); // Logger

    /**
     * Endpoint to retrieve information about the currently authenticated user.
     *
     * @return ResponseEntity containing the UserDto with user information and HttpStatus indicating the result.
     */
    @GetMapping("/get")
    public ResponseEntity<?> getUser() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        UserDto userDto = mapper.convertToDto(user);
        logger.info("User data for email " + userDto.getEmail() + " was got successfully");
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
    public ResponseEntity<String> deleteUser(@RequestBody RegisterAndLoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPasswordHash()));

            if (authentication.isAuthenticated()) {
                User user = userService.getUserByEmail(loginRequest.getEmail());

                confirmationTokenService.deleteConfirmationTokensByUserId(user.getUserId());
                logger.info("Confirmation tokens for user " + user.getEmail() + " were deleted");

                List<Account> accounts = accountService.getAccountsByUserId(user.getUserId());
                for (Account account : accounts) {
                    transactionService.deleteTransactionsByAccountId(account.getAccountId());
                    logger.info("Transactions of user " + user.getEmail() + " and account with ID " + account.getAccountId() + " were deleted");

                    AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
                    accountUsersService.deleteAccountUsersById(accountUsers.getAccountUsersId());
                    logger.info("AccountUsers with ID " + accountUsers.getAccountUsersId() + " of user " + user.getEmail() + " were deleted");
                }

                accountUsersService.deleteUserIdFromAccountUsers(user.getUserId());
                logger.info("User with email " + user.getEmail() + " was deleted from AccountUsers");

                accountService.deleteAccountsByUserId(user.getUserId());
                logger.info("Accounts of user with email " + user.getEmail() + " were deleted");

                userService.deleteUserById(user.getUserId());
                logger.info("User with email " + user.getEmail() + " was deleted");

                return ResponseEntity.status(HttpStatus.OK).body("User is deleted!");
            } else {
                logger.error("Incorrect user data with email " + loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect user data!");
            }
        } catch (DisabledException e) {
            logger.error("Email " + loginRequest.getEmail() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }
        catch (AuthenticationException e) {
            logger.error("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Endpoint to change the password for authenticated user.
     *
     * @param changePasswordRequest Request for change password
     * @return ResponseEntity with a success message and HttpStatus indicating the result.
     */
    @PatchMapping("/change-password")
    public ResponseEntity<String> changeUserPassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPasswordHash())) {
            logger.error("Incorrect password for email " + customUserDetails.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect password!");
        }

        user.setPasswordHash(
                passwordEncoder.encode(changePasswordRequest.getNewPassword())
        );
        userService.updateUser(user);
        logger.error("Password was changed for email " + user.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body("Password was changed!");
    }

    /**
     * Endpoint to reset the password for user.
     *
     * @param email User email.
     * @return ResponseEntity with a success message and HttpStatus indicating the result.
     */
    @PatchMapping("/reset-password")
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<String> resetUserPassword(@RequestParam String email) {
        if (!userValidator.checkEmail(email)) {
            logger.error("Invalid email format for email" + email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format!");
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            logger.error("No user with email " + email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No user with this email!");
        }
        if (!user.getIsVerified()) {
            logger.error("Email " + user.getEmail() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User email is not verified!");
        }

        PasswordManager passwordManager = new PasswordManager();
        String generatedPassword = passwordManager.generatePassword(10);
        String hashedPassword = passwordManager.hashPassword(generatedPassword, user.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(hashedPassword)
        );
        userService.updateUser(user);

        logger.info("User password for email " + user.getEmail() + " was reset");

        emailSenderService.sendEmail(user.getEmail(), SUBJECT, buildPasswordEmail(generatedPassword));

        return ResponseEntity.status(HttpStatus.OK).body("Password was changed. Check your email!");
    }

    /**
     * Build an HTML email with a new password.
     *
     * @param newPassword The new password to be included in the email.
     * @return The HTML email content.
     */
    private String buildPasswordEmail(String newPassword) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Password Reset</title>
                  <style>
                    body {
                      font-family: Arial, sans-serif;
                      margin: 0;
                      padding: 0;
                      background-color: #f4f4f4;
                    }
                    .container {
                      max-width: 600px;
                      margin: 20px auto;
                      background-color: #ffffff;
                      padding: 20px;
                      border-radius: 8px;
                      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                      text-align: center; /* Center-align text within the container */
                    }
                    .header {
                      margin-bottom: 20px;
                    }
                    .content {
                      margin-bottom: 30px;
                    }
                    .footer {
                      margin-top: 20px;
                      color: #777777;
                    }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h2>Password Reset</h2>
                    </div>
                    <div class="content">
                      <p>Your password has been reset. Here is your new password:</p>
                      <p><strong>%s</strong></p>
                      <p>For security reasons, please change your password after logging in.</p>
                    </div>
                    <div class="footer">
                      <p>Budgetracker</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(newPassword);
    }

}
