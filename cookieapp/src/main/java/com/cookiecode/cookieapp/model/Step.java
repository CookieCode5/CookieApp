package com.cookiecode.cookieapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter 
public class Step {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    private int stepOrder;
    private String title;
    @Column(columnDefinition = "TEXT") private String instruction;
    private int durationMinutes;
    private String tip;
    private boolean parallelizable;
    // getters / setters
}