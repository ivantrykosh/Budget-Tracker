package com.ivantrykosh.app.budgettracker.server.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Token response class
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TokenResponse {
    private String token;
}
