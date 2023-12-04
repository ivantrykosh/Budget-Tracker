package com.ivantrykosh.app.budgettracker.server.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Change password request class
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ChangePasswordRequest {
    private String newPassword; // New password
}
