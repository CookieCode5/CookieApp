package com.cookiecode.cookieapp.repository;

import com.cookiecode.cookieapp.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findAllByOrderByCreatedAtDesc();
    Page<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);
}