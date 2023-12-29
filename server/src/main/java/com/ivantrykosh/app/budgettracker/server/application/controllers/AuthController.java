package com.ivantrykosh.app.budgettracker.server.application.controllers;

import com.ivantrykosh.app.budgettracker.server.infrastructure.email.EmailSenderService;
import com.ivantrykosh.app.budgettracker.server.domain.model.Account;
import com.ivantrykosh.app.budgettracker.server.domain.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.domain.model.ConfirmationToken;
import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import com.ivantrykosh.app.budgettracker.server.presentation.requests.RegisterAndLoginRequest;
import com.ivantrykosh.app.budgettracker.server.presentation.responses.TokenResponse;
import com.ivantrykosh.app.budgettracker.server.application.services.AccountService;
import com.ivantrykosh.app.budgettracker.server.application.services.AccountUsersService;
import com.ivantrykosh.app.budgettracker.server.application.services.ConfirmationTokenService;
import com.ivantrykosh.app.budgettracker.server.util.CustomUserDetails;
import com.ivantrykosh.app.budgettracker.server.util.JwtUtil;
import com.ivantrykosh.app.budgettracker.server.application.services.UserService;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Auth REST controller
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private ConfirmationTokenService confirmationTokenService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountUsersService accountUsersService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailSenderService emailSenderService;
    private final UserValidator userValidator = new UserValidator();
    private final String LINK = "http://192.168.1.7:8080/api/v1/auth/confirm?token="; // Confirmation link
    private final String SUBJECT = "Confirm your email address"; // Email subject
    Logger logger = LoggerFactory.getLogger(AuthController.class); // Logger

    /**
     * Endpoint for user registration. Registers a new user, generates a confirmation token,
     * and initiates the email confirmation process.
     *
     * @param registerRequest The user data including email, password for registration.
     * @return ResponseEntity with a success message and HttpStatus indicating the result.
     */
    @PostMapping("/register")
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<String> registerUser(@RequestBody RegisterAndLoginRequest registerRequest) {
        if (!userValidator.checkEmail(registerRequest.getEmail())) {
            logger.error("Invalid email format: " + registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format!");
        }

        if (userService.getUserByEmail(registerRequest.getEmail()) != null) {
            logger.error("Email " + registerRequest.getEmail() + " is already used");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already used!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPasswordHash()));
        user.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        user.setIsVerified(false);

        User savedUser = userService.saveUser(user);

        logger.info("User with email " + registerRequest.getEmail() + " was created");

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmationToken(token);
        confirmationToken.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        confirmationToken.setExpiresAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15)));
        confirmationToken.setUser(savedUser);

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        logger.info("Confirmation token for email " + savedUser.getEmail() + " was created");

        emailSenderService.sendEmail(savedUser.getEmail(), SUBJECT, buildConfirmationEmail(LINK + token));

        Account account = new Account();
        account.setName("My wallet");
        account.setUser(savedUser);
        Account savedAccount = accountService.saveAccount(account);

        logger.info("Account with name " + account.getName() + " of user " + user.getEmail() + " was saved");

        AccountUsers accountUsers = new AccountUsers();
        accountUsers.setAccount(savedAccount);
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        logger.info("AccountUsers with ID " + savedAccountUsers.getAccountUsersId() + " of user " + user.getEmail() + " was saved");

        return ResponseEntity.status(HttpStatus.CREATED).body("User was created! Please, confirm the user email address!");
    }

    /**
     * Endpoint for user login. Authenticates the user and generates a JWT token upon successful login.
     *
     * @param loginRequest The user data including email and password for login.
     * @return ResponseEntity with the JWT token in the body and HttpStatus indicating the result.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody RegisterAndLoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPasswordHash()));

            if (authentication.isAuthenticated()) {
                logger.info("User with email " + loginRequest.getEmail() + " successfully logged in");
                return ResponseEntity.status(HttpStatus.OK).body(
                        new TokenResponse(
                                jwtUtil.generateToken(loginRequest.getEmail())
                        )
                );
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
     * Endpoint to confirm a user's email using a confirmation token.
     *
     * @param token The confirmation token received via email.
     * @return ResponseEntity with a confirmation message and HttpStatus indicating the result.
     */
    @GetMapping("/confirm")
    @Transactional
    public ResponseEntity<String> confirmToken(@RequestParam String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getConfirmationTokenByConfirmationToken(token);

        if (confirmationToken == null) {
            logger.error("Invalid confirmation token " + token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid confirmation token!");
        }
        if (confirmationToken.getConfirmedAt() != null) {
            logger.error("Email is already confirmed. Confirmation token: " + token);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already confirmed!");
        }
        Date dateNow = Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
        if (dateNow.after(confirmationToken.getExpiresAt())) {
            logger.error("Confirmation token " + token + " has expired");
            return ResponseEntity.status(HttpStatus.GONE).body("Confirmation token has expired! Please, login and click OK to send confirmation email!");
        }

        confirmationToken.setConfirmedAt(dateNow);
        confirmationTokenService.updateConfirmationToken(confirmationToken);

        User user = userService.getUserById(confirmationToken.getUser().getUserId());
        user.setIsVerified(true);
        userService.updateUser(user);

        logger.info("User email " + user.getEmail() + " is confirmed");

        return ResponseEntity.status(HttpStatus.OK).body("User email is confirmed. You can close this tab!");
    }

    /**
     * Endpoint to refresh an authentication token.
     * Retrieves the current username from the authenticated user and generates a new JWT token.
     *
     * @return ResponseEntity with the new JWT token in the body and HttpStatus OK.
     */
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Token is refreshed for user email " + username);
        return ResponseEntity.status(HttpStatus.OK).body(
                new TokenResponse(
                        jwtUtil.generateToken(username)
                )
        );
    }

    /**
     * Endpoint to send a confirmation email.
     *
     * @return ResponseEntity with a confirmation message and HttpStatus indicating the result.
     */
    @PostMapping("/send-confirmation-email")
    @Transactional
    public ResponseEntity<String> sendConfirmationEmail(@RequestBody RegisterAndLoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPasswordHash()));

            if (authentication.isAuthenticated()) {
                logger.error("Email " + loginRequest.getEmail() + " is already verified");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User email is already verified!");
            } else {
                logger.error("Incorrect user data for email " + loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect user data!");
            }
        } catch (DisabledException e) {
            User user = userService.getUserByEmail(loginRequest.getEmail());

            List<ConfirmationToken> confirmationTokens = confirmationTokenService.getConfirmationTokensByUserId(user.getUserId());
            List<ConfirmationToken> newConfirmationTokens = confirmationTokens.stream()
                    .filter(token -> token.getCreatedAt()
                            .after(
                                    Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10))
                            )
                    ).collect(Collectors.toList());
            if (!newConfirmationTokens.isEmpty()) {
                logger.info("Confirmation token for " + loginRequest.getEmail() + " is already sent");
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Confirmation email is already sent!");
            }

            String token = UUID.randomUUID().toString();

            ConfirmationToken confirmationToken = new ConfirmationToken();
            confirmationToken.setConfirmationToken(token);
            confirmationToken.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
            confirmationToken.setExpiresAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15)));
            confirmationToken.setUser(user);

            confirmationTokenService.saveConfirmationToken(confirmationToken);

            logger.info("Confirmation token for email " + user.getEmail() + " was created");

            emailSenderService.sendEmail(user.getEmail(), SUBJECT, buildConfirmationEmail(LINK + token));

            return ResponseEntity.status(HttpStatus.CREATED).body("Email was sent. Confirm your email address!");
        }
        catch (AuthenticationException e) {
            logger.error("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Build a confirmation HTML email for activation.
     *
     * @param link The activation link.
     * @return The confirmation HTML email content.
     */
    private String buildConfirmationEmail(String link) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Email Confirmation</title>
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
                      text-align: center; /* Center the content */
                    }
                    .header {
                      margin-bottom: 20px;
                    }
                    .content {
                      margin-bottom: 30px;
                    }
                    .button {
                      display: inline-block;
                      padding: 10px 20px;
                      background-color: #007bff;
                      color: #ffffff;
                      text-decoration: none;
                      border-radius: 5px;
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
                      <h2>Email Confirmation</h2>
                    </div>
                    <div class="content">
                      <p>Thank you for registering. Please click on the below link to activate your account:</p>
                      <a href="%s" class="button">Activate Now</a>
                      <p>Link will expire in 15 minutes.</p>
                    </div>
                    <div class="footer">
                      <p>App Name: Budgetracker</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(link);
    }
}
