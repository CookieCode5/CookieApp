package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.ReviewForm;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.service.RecipeService;
import com.cookiecode.cookieapp.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RecipeController {

    private final RecipeService recipeService;
    private final ReviewService reviewService;

    public RecipeController(RecipeService recetteService, ReviewService reviewService) {
        this.recipeService = recetteService;
        this.reviewService = reviewService;
    }

    /** Liste des recettes (lecture seule). Recherche et filtres : Sprint 3. */
    @GetMapping("/recipes")
    public String liste(Model model) {
        var recipes = recipeService.lister();
        model.addAttribute("recettes", recipes);
        model.addAttribute("nbRecettes", recipes.size());
        return "recipes/liste";
    }

    @GetMapping("/recipes/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Recipe recipe = recipeService.getById(id);
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        boolean isAuthor = me != null && recipe.getAuthor() != null && recipe.getAuthor().getId() == me.getId();
    
        model.addAttribute("recipe", recipe);
        model.addAttribute("reviews", reviewService.forRecipe(id));
        model.addAttribute("stars", reviewService.starDistribution(id));
        model.addAttribute("canReview", me != null && !isAuthor);
    
        // Pre-fill the form with my previous review, if any (RG-A1: the new one replaces it)
        ReviewForm form = new ReviewForm();
        if (me != null) {
            reviewService.findMine(me.getId(), id).ifPresent(r -> {
                form.setRating(r.getRating());
                form.setComment(r.getComment());
            });
        }
        model.addAttribute("reviewForm", form);
        return "recipes/details";
    }
}
