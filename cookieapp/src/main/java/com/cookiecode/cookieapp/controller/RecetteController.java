package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.service.RecetteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecetteController {

    private final RecetteService recetteService;

    public RecetteController(RecetteService recetteService) {
        this.recetteService = recetteService;
    }

    /** Liste des recettes (lecture seule). Recherche et filtres : Sprint 3. */
    @GetMapping("/recettes")
    public String liste(Model model) {
        var recettes = recetteService.lister();
        model.addAttribute("recettes", recettes);
        model.addAttribute("nbRecettes", recettes.size());
        return "recettes/liste";
    }
}
