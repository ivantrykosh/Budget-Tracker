package com.ivantrykosh.app.budgettracker.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Transaction entity
 */
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId; // Transaction ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // Account to which transaction belongs

    @Column(name = "category", nullable = false)
    private String category; // Category of transaction

    @Column(name = "value", nullable = false)
    private Double value; // Value of transaction

    @Column(name = "date", nullable = false)
    private Date date; // Date of transaction

    @Column(name = "to_from_whom")
    private String toFromWhom; // Transaction from whom or to whom

    @Column(name = "note")
    private String note; // Transaction note
}
