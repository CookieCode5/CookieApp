package com.cookiecode.cookieapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.GenerationType;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "recipe_id"}))

@Entity
@Getter 
@Setter 
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipe_id") private Recipe recipe;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    private int rating;
    @Column(columnDefinition = "TEXT") private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}
