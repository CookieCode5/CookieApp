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

    public List<Recipe> dernieres(int n, String filter) {
        String f = filter == null ? "" : filter;
        return RecipeRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> switch (f) {
                    case "vegetarian" -> r.isVegetarian();
                    case "express" -> r.totalTime() <= EXPRESS_MAX_MINUTES;
                    // case "budget" -> priceService.costPerServing(r) <= 3  → once AmiraO's PriceService exists
                    default -> true;
                })
                .limit(n)
                .toList();
    }

    public long compter() {
        return RecipeRepository.count();
    }

    public Recipe getById(Long id) {
        return RecipeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }

    private static final int EXPRESS_MAX_MINUTES = 20; // to decide with the team: no seeded recipe is ≤ 20 min


}