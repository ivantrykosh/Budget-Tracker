package com.ivantrykosh.app.budgettracker.server.presentation.responses;

import com.ivantrykosh.app.budgettracker.server.application.dtos.AccountDto;
import com.ivantrykosh.app.budgettracker.server.application.dtos.AccountUsersDto;
import lombok.*;

/**
 * Account response class
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountResponse {
    private AccountDto accountDto; // Account
    private AccountUsersDto accountUsersDto; // Account users
}
