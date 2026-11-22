package com.cookiecode.cookieapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cookiecode.cookieapp.model.Allergen;

public interface AllergenRepository extends JpaRepository<Allergen,Long>{

}
