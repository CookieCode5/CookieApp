package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.model.Recette;
import com.cookiecode.cookieapp.repository.RecetteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecetteService {

    private final RecetteRepository recetteRepository;

    public RecetteService(RecetteRepository recetteRepository) {
        this.recetteRepository = recetteRepository;
    }

    /** Toutes les recettes, les plus récentes d'abord (page /recettes). */
    public List<Recette> lister() {
        return recetteRepository.findAllByOrderByDateCreationDesc();
    }

    /** Les n recettes les plus récentes (page d'accueil). */
    public List<Recette> dernieres(int n) {
        return recetteRepository.findAllByOrderByDateCreationDesc(PageRequest.of(0, n));
    }

    public long compter() {
        return recetteRepository.count();
    }
}
