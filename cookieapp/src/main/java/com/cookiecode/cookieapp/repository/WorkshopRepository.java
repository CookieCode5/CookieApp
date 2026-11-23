package com.cookiecode.cookieapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cookiecode.cookieapp.model.Workshop;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    // Groups the user belongs to, as organizer or participant (left join: a group may have no participant yet)
    @Query("""
           select distinct w from Workshop w left join w.participants p
           where w.organizer.id = :userId or p.id = :userId
           order by w.createdAt desc""")
    List<Workshop> findByMember(@Param("userId") Long userId);

    Optional<Workshop> findByInviteCode(String inviteCode);
}
