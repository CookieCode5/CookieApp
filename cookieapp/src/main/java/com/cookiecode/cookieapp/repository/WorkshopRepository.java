package com.cookiecode.cookieapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cookiecode.cookieapp.model.Workshop;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    List<Workshop> findAllByOrderByCreatedAtDesc();
}