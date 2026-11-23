package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.service.RecipeService;
import com.cookiecode.cookieapp.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private static final int NB_RECETTES_ACCUEIL = 6;

    private final RecipeService recetteService;
    private final ReviewService reviewService;

    public HomeController(RecipeService recetteService, ReviewService reviewService) {
        this.recetteService = recetteService;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String accueil(@RequestParam(required = false) String filter, Model model) {
        model.addAttribute("recettes", recetteService.dernieres(NB_RECETTES_ACCUEIL, filter));
        model.addAttribute("nbRecettes", recetteService.compter());
        model.addAttribute("topRecipes", reviewService.topOfTheWeek(3));
        model.addAttribute("filter", filter);
        return "index";
    }
}
