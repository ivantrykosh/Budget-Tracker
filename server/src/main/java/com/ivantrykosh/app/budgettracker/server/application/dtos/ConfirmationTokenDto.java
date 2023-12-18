package com.ivantrykosh.app.budgettracker.server.application.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Confirmation token dto
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ConfirmationTokenDto {
    private Long confirmationTokenId; // Confirmation token ID

    private Long userId; // ID of user to whom confirmation token belongs

    private String confirmationToken; // Confirmation token

    private Date createdAt; // Date the token was created

    private Date expiresAt; // Date the token is expired

    private Date confirmedAt; // Date the token was confirmed
}
