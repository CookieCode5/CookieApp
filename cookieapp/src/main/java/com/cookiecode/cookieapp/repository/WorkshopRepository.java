package com.cookiecode.cookieapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import com.cookiecode.cookieapp.model.Workshop;

public interface WorkshopRepository extends JpaRepository<Workshop,Long>{

    List<Workshop> findByScheduledAtAfterOrderByScheduledAt(LocalDateTime LocalDateTime);

}
