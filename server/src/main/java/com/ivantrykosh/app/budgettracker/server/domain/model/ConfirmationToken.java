package com.ivantrykosh.app.budgettracker.server.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Confirmation token entity
 */
@Entity
@Table(name = "confirmation_tokens")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "confirmation_token_id")
    private Long confirmationTokenId; // Confirmation token ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User to whom confirmation token belongs

    @Column(name = "confirmationToken", nullable = false)
    private String confirmationToken; // Confirmation token

    @Column(name = "created_at", nullable = false)
    private Date createdAt; // Date the token was created

    @Column(name = "expires_at", nullable = false)
    private Date expiresAt; // Date the token is expired

    @Column(name = "confirmed_at")
    private Date confirmedAt; // Date the token was confirmed
}
