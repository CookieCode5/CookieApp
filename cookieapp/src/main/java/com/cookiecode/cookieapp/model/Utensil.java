package com.cookiecode.cookieapp.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter 
@Setter 
public class Utensil {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true) private String name;
    private boolean common;

    @ManyToMany(mappedBy = "utensils")
    @ToString.Exclude
    private Set<User> users = new HashSet<>();
}
