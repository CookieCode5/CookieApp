package com.cookiecode.cookieapp.repository;

import com.cookiecode.cookieapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Provisoire : à fusionner avec celui d'Amina / Danika. */
public interface UtilisateurRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
