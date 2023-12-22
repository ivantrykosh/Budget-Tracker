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
import com.ivantrykosh.app.budgettracker.server.application.services.*;
import com.ivantrykosh.app.budgettracker.server.util.CustomUserDetails;
import com.ivantrykosh.app.budgettracker.server.util.PasswordManager;
import com.ivantrykosh.app.budgettracker.server.validators.UserValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Autowired
    private EmailSenderService emailSenderService;
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
        logger.info("User data for email" + userDto.getEmail() + " was got successfully");
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
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

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
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">New Password</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Your password has been reset. Here is your new password:</p>\n" +
                "            <blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\">\n" +
                "                <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">" + newPassword + "</p>\n" +
                "            </blockquote>\n" +
                "            <p>For security reasons, please change your password after logging in.</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

}
