package com.cookiecode.cookieapp.repository;

import com.cookiecode.cookieapp.model.Recette;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetteRepository extends JpaRepository<Recette, Long> {

    /** Toutes les recettes, les plus récentes d'abord (auteur chargé pour éviter le N+1). */
    @EntityGraph(attributePaths = "auteur")
    List<Recette> findAllByOrderByDateCreationDesc();

    /** Les N dernières recettes : on passe PageRequest.of(0, n). */
    @EntityGraph(attributePaths = "auteur")
    List<Recette> findAllByOrderByDateCreationDesc(Pageable pageable);
}
