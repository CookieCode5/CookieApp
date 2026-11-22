package com.cookiecode.cookieapp.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.cookiecode.cookieapp.model.enums.Aisle;
import com.cookiecode.cookieapp.model.enums.Unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter 
public class Ingredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING) private Unit referenceUnit;
    @Column(precision = 10, scale = 2) private BigDecimal pricePerUnit;
    @Enumerated(EnumType.STRING) private Aisle aisle;

    @ManyToMany
    @JoinTable(name = "ingredient_allergen",
        joinColumns = @JoinColumn(name = "ingredient_id"),
        inverseJoinColumns = @JoinColumn(name = "allergen_id"))
    private Set<Allergen> allergens = new HashSet<>();
    // getters / setters
}