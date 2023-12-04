package com.ivantrykosh.app.budgettracker.server.responses;

import com.ivantrykosh.app.budgettracker.server.dtos.AccountDto;
import com.ivantrykosh.app.budgettracker.server.dtos.AccountUsersDto;
import lombok.*;

/**
 * Account response class
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountResponse {
    private AccountDto account; // Account
    private AccountUsersDto accountUsersDto; // Account users
}
