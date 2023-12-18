package com.ivantrykosh.app.budgettracker.server.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * User entity
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // User ID

    @Column(name = "email", length = 320, nullable = false, unique = true)
    private String email; // User email

    @Column(name = "user_password_hash", nullable = false)
    private String passwordHash; // Password hash

    @Column(name = "registration_date", nullable = false)
    private Date registrationDate; // Date of registration

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified; // Is user's email verified
}
