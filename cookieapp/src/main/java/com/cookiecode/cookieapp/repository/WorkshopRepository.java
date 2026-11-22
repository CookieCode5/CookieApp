package com.cookiecode.cookieapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import com.cookiecode.cookieapp.model.enums.Workshop;

public interface WorkshopRepository extends JpaRepository<Workshop,Long>{

    List<Workshop> findByScheduledAtAfterOrderByScheduledAt(LocalDate LocalDateTime);

}
