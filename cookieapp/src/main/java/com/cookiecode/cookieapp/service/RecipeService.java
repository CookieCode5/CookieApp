package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository RecipeRepository;

    public RecipeService(RecipeRepository RecipeRepository) {
        this.RecipeRepository = RecipeRepository;
    }

    public List<Recipe> lister() {
        return RecipeRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Recipe> dernieres(int n) {
        return RecipeRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, n)).getContent();
    }

    public long compter() {
        return RecipeRepository.count();
    }

    public Recipe getById(Long id) {
        return RecipeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }
}