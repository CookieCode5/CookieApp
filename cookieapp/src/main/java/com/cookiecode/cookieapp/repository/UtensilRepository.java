package com.cookiecode.cookieapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cookiecode.cookieapp.model.Utensil;

public interface UtensilRepository extends JpaRepository<Utensil, Long> {
    
}
