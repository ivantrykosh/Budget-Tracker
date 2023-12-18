package com.ivantrykosh.app.budgettracker.server.application.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * User dto
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDto {
    private Long userId; // User ID

    private String email; // User email

    private Date registrationDate; // Date of registration

    private Boolean isVerified; // Is user's email verified
}
