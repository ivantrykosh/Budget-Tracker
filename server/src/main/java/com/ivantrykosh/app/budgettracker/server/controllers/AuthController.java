package com.ivantrykosh.app.budgettracker.server.controllers;

import com.ivantrykosh.app.budgettracker.server.model.ConfirmationToken;
import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.requests.RegisterAndLoginRequest;
import com.ivantrykosh.app.budgettracker.server.services.ConfirmationTokenService;
import com.ivantrykosh.app.budgettracker.server.util.JwtUtil;
import com.ivantrykosh.app.budgettracker.server.services.UserService;
import com.ivantrykosh.app.budgettracker.server.validators.UserValidator;
import jakarta.transaction.Transactional;
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
import java.util.UUID;

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
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final UserValidator userValidator = new UserValidator();

    /**
     * Endpoint for user registration. Registers a new user, generates a confirmation token,
     * and initiates the email confirmation process.
     *
     * @param registerRequest The user data including email, password for registration.
     * @return ResponseEntity with a success message and HttpStatus indicating the result.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterAndLoginRequest registerRequest) {
        if (!userValidator.checkEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPasswordHash()));
        user.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        user.setIsVerified(false);

        User savedUser = userService.saveUser(user);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmationToken(token);
        confirmationToken.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        confirmationToken.setExpiresAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15)));
        confirmationToken.setUser(savedUser);

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        // todo send email

        return ResponseEntity.status(HttpStatus.OK).body("User was created! Please, confirm the user email!" + token);
    }

    /**
     * Endpoint for user login. Authenticates the user and generates a JWT token upon successful login.
     *
     * @param loginRequest The user data including email and password for login.
     * @return ResponseEntity with the JWT token in the body and HttpStatus indicating the result.
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody RegisterAndLoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPasswordHash()));

            if (authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        jwtUtil.generateToken(loginRequest.getEmail())
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect user data!");
            }
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }
        catch (AuthenticationException e) {
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid confirmation token!");
        }
        if (confirmationToken.getConfirmedAt() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already confirmed!");
        }
        Date dateNow = Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
        if (dateNow.after(confirmationToken.getExpiresAt())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Confirmation token has expired!");
        }

        confirmationToken.setConfirmedAt(dateNow);
        confirmationTokenService.updateConfirmationToken(confirmationToken);

        User user = userService.getUserById(confirmationToken.getUser().getUserId());
        user.setIsVerified(true);
        userService.updateUser(user);

        return ResponseEntity.status(HttpStatus.OK).body("User email is confirmed!");
    }

    /**
     * Endpoint to refresh an authentication token.
     * Retrieves the current username from the authenticated user and generates a new JWT token.
     *
     * @return ResponseEntity with the new JWT token in the body and HttpStatus OK.
     */
    @GetMapping("/refresh")
    public ResponseEntity<String> refreshToken() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.OK).body(
                jwtUtil.generateToken(username)
        );
    }
}
