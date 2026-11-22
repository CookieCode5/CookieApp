package com.cookiecode.cookieapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cookiecode.cookieapp.model.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
}
