package com.cookiecode.cookieapp.repository;

import com.cookiecode.cookieapp.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Provisoire : à fusionner avec celui d'Amina / Danika. */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}
