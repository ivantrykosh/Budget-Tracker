package com.ivantrykosh.app.budgettracker.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Account users entity
 */
@Entity
@Table(name = "account_users")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AccountUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_users_id")
    private Long accountUsersId; // Account users ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // Account to which users belong

    @Column(name = "user2_id", nullable = false)
    private Long user2Id; // Second user id

    @Column(name = "user3_id", nullable = false)
    private Long user3Id; // Third user id

    @Column(name = "user4_id", nullable = false)
    private Long user4Id; // Fourth user id
}
