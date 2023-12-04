package com.ivantrykosh.app.budgettracker.server.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Create account request class
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CreateAndChangeAccountRequest {
    private String name; // Account name
    private String email2; // User 2 email
    private String email3; // User 3 email
    private String email4; // User 4 email
}
