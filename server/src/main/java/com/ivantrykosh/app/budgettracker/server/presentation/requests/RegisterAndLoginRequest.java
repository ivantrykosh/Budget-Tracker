package com.ivantrykosh.app.budgettracker.server.presentation.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Register and login request class
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegisterAndLoginRequest {
    private String email; // User email
    private String passwordHash; // User password hash
}
