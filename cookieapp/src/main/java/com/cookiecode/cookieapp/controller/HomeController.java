package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.service.RecipeService;
import com.cookiecode.cookieapp.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private static final int HOME_RECIPE_COUNT = 6;

    private final RecipeService recipeService;
    private final ReviewService reviewService;

    public HomeController(RecipeService recipeService, ReviewService reviewService) {
        this.recipeService = recipeService;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String filter, Model model) {
        model.addAttribute("recipes", recipeService.latest(HOME_RECIPE_COUNT, filter));
        model.addAttribute("recipeCount", recipeService.count());
        model.addAttribute("topRecipes", reviewService.topOfTheWeek(3));
        model.addAttribute("filter", filter);
        return "index";
    }
}