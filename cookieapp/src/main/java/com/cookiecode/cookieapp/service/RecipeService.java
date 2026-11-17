package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.repository.RecetteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecipeService {

    private final RecetteRepository recetteRepository;

    public RecipeService(RecetteRepository recetteRepository) {
        this.recetteRepository = recetteRepository;
    }

    public List<Recipe> lister() {
        return recetteRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Recipe> dernieres(int n) {
        return recetteRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, n));
    }

    public long compter() {
        return recetteRepository.count();
    }
}