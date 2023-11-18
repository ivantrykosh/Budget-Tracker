package com.ivantrykosh.app.budgettracker.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Category entity
 */
@Entity
@Table(name = "categories")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId; // Category ID

    @Column(name = "category_type", nullable = false)
    private Byte categoryType; // Type of category (-1 if category is expense and 1 if category is income)

    @Column(name = "category", nullable = false)
    private String name; // Name of category
}
