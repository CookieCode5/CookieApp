package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.service.RecetteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilController {

    private static final int NB_RECETTES_ACCUEIL = 6;

    private final RecetteService recetteService;

    public AccueilController(RecetteService recetteService) {
        this.recetteService = recetteService;
    }

    @GetMapping("/")
    public String accueil(Model model) {
        model.addAttribute("recettes", recetteService.dernieres(NB_RECETTES_ACCUEIL));
        model.addAttribute("nbRecettes", recetteService.compter());
        return "index";
    }
}
