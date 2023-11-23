package com.ivantrykosh.app.budgettracker.server.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Account dto
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AccountDto {
    private Long accountId; // Account ID

    private Long userId; // ID of user to whom account belongs

    private String name; // Name of account

    private Double incomesSum; // Sum of incomes

    private Double expensesSum; // Sum of expenses
}
