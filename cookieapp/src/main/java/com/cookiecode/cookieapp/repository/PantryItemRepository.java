package com.cookiecode.cookieapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cookiecode.cookieapp.model.PantryItem;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {
    
}
