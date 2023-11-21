package com.ivantrykosh.app.budgettracker.server.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Transaction dto
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TransactionDto {
    private Long transactionId; // Transaction ID

    private Long accountId; // ID of account to which transaction belongs

    private String category; // Category of transaction

    private Double value; // Value of transaction

    private Date date; // Date of transaction

    private String toFromWhom; // Transaction from whom or to whom

    private String note; // Transaction note
}
