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

    private Long user2Id; // Second user id

    private Long user3Id; // Third user id

    private Long user4Id; // Fourth user id
}
