package com.cookiecode.cookieapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * VERSION MINIMALE (provisoire) : champs simples + auteur, d'après le diagramme 1 de l'UML.
 * A fusionner avec l'entité complète d'AmiraT (etapes, composition, ustensiles, avis).
 * Pas de @Data sur les entités liées : @Getter @Setter seulement.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Recette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(length = 2000)
    private String description;

    /** Chemin ou URL de l'image (fichier dans /uploads), jamais un byte[] en base. */
    private String imageUrl;

    private int nbPersonnesBase = 4;
    private int tempsPreparation;
    private int tempsCuisson;

    @Enumerated(EnumType.STRING)
    private Difficulte difficulte;

    @Enumerated(EnumType.STRING)
    private Categorie categorie;

    private boolean vegetarien;
    private boolean vegan;
    private boolean premium;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    public int tempsTotal() {
        return tempsPreparation + tempsCuisson;
    }
}
