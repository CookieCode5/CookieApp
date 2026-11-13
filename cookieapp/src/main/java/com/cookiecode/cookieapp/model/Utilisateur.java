package com.cookiecode.cookieapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * VERSION MINIMALE (provisoire) : juste ce qu'il faut pour afficher l'auteur d'une recette.
 * A remplacer par l'entité complète de Danika (abonnement 1-1, favoris, frigo, allergies...).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pseudo;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.UTILISATEUR;

    private LocalDate dateInscription = LocalDate.now();
}
