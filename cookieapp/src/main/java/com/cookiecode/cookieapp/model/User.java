package com.cookiecode.cookieapp.model;
import com.cookiecode.cookieapp.model.enums.Role;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Table;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import lombok.ToString;

@Getter
@Setter 
@ToString 
@Entity 
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (unique=true)
    private String username;

    @Column  (unique=true)
    private String email;

    @Column
    private String password_hash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private LocalDate registration_date;

    @ManyToMany
    @JoinTable (
        name = "user_utensil",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "utensil_id")
    )
    @ToString.Exclude
    private Set<Utensil> utensils = new HashSet<>();

    @ManyToMany
    @JoinTable (
        name = "favorites",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    @ToString.Exclude
    private Set<Recipe> favorites = new HashSet<>();

    @ManyToMany
    @JoinTable (
        name = "user_allergen",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    @ToString.Exclude
    private Set<Allergen> allergies = new HashSet<>();
    
}
