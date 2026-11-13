package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.service.RecipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recetteService) {
        this.recipeService = recetteService;
    }

    /** Liste des recettes (lecture seule). Recherche et filtres : Sprint 3. */
    @GetMapping("/recipes")
    public String liste(Model model) {
        var recipes = recipeService.lister();
        model.addAttribute("recettes", recipes);
        model.addAttribute("nbRecettes", recipes.size());
        return "recipes/liste";
    }
}
