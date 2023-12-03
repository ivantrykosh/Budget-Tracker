package com.ivantrykosh.app.budgettracker.server.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Account users dto
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AccountUsersDto {
    private Long accountUsersId; // Account users ID

    private Long accountId; // ID of account to which users belong

    private String email2; // Second user email

    private String email3; // Third user email

    private String email4; // Fourth user email
}
