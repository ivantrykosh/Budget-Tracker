package com.ivantrykosh.app.budgettracker.server.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Account entity
 */
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId; // Account ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User to whom account belongs

    @Column(name = "name", nullable = false)
    private String name; // Name of account
}
