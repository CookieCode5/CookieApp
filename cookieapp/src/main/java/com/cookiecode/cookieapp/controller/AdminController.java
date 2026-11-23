package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.model.Allergen;
import com.cookiecode.cookieapp.model.Ingredient;
import com.cookiecode.cookieapp.model.Utensil;
import com.cookiecode.cookieapp.model.enums.Aisle;
import com.cookiecode.cookieapp.model.enums.Unit;
import com.cookiecode.cookieapp.repository.AllergenRepository;
import com.cookiecode.cookieapp.repository.IngredientRepository;
import com.cookiecode.cookieapp.repository.UtensilRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final IngredientRepository ingredientRepository;
    private final UtensilRepository utensilRepository;
    private final AllergenRepository allergenRepository;

    public AdminController(IngredientRepository ingredientRepository, UtensilRepository utensilRepository, AllergenRepository allergenRepository) {
        this.ingredientRepository = ingredientRepository;
        this.utensilRepository = utensilRepository;
        this.allergenRepository = allergenRepository;
    }

    private boolean isNotAdmin(HttpSession session) {
        Object sessionUser = session.getAttribute("user");
        if (sessionUser instanceof PublicUser publicUser) {
            return !publicUser.isAdmin();
        }
        return true;
    }

    private void loadAdminModel(Model model, String activeTab) {
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("ingredients", ingredientRepository.findAll());
        model.addAttribute("utensils", utensilRepository.findAll());
        model.addAttribute("allergens", allergenRepository.findAll());
        model.addAttribute("units", Unit.values());
        model.addAttribute("aisles", Aisle.values());
    }

    @GetMapping("/ingredients")
    public String getIngredients(@RequestParam(value = "editId", required = false) Long editId, Model model, HttpSession session) {
        if (isNotAdmin(session)) {
            return "redirect:/login";
        }
        loadAdminModel(model, "ingredients");
        Ingredient form = (editId != null) ? ingredientRepository.findById(editId).orElseGet(Ingredient::new) : new Ingredient();
        model.addAttribute("ingredientForm", form);
        return "admin";
    }

    @PostMapping("/ingredients")
    public String postIngredients(@ModelAttribute("ingredientForm") Ingredient ingredient, @RequestParam(value = "allergenIds", required = false) List<Long> allergenIds, @RequestParam(value = "deleteId", required = false) Long deleteId, HttpSession session) {
        if (isNotAdmin(session)) {
            return "redirect:/login";
        }
        if (deleteId != null) {
            ingredientRepository.deleteById(deleteId);
            return "redirect:/admin/ingredients";
        }

        Set<Allergen> selectedAllergens = new HashSet<>();
        if (allergenIds != null && !allergenIds.isEmpty()) {
            selectedAllergens.addAll(allergenRepository.findAllById(allergenIds));
        }
        ingredient.setAllergens(selectedAllergens);

        ingredientRepository.save(ingredient);
        return "redirect:/admin/ingredients";
    }

    @GetMapping("/utensils")
    public String getUtensils(@RequestParam(value = "editId", required = false) Long editId, Model model, HttpSession session) {
        if (isNotAdmin(session)) {
            return "redirect:/login";
        }
        loadAdminModel(model, "utensils");
        Utensil form = (editId != null) ? utensilRepository.findById(editId).orElseGet(Utensil::new) : new Utensil();
        model.addAttribute("utensilForm", form);
        return "admin";
    }

    @PostMapping("/utensils")
    public String postUtensils(@ModelAttribute("utensilForm") Utensil utensil, @RequestParam(value = "deleteId", required = false) Long deleteId, HttpSession session) {
        if (isNotAdmin(session)) {
            return "redirect:/login";
        }
        if (deleteId != null) {
            utensilRepository.deleteById(deleteId);
            return "redirect:/admin/utensils";
        }

        utensilRepository.save(utensil);
        return "redirect:/admin/utensils";
    }

    @GetMapping("/allergens")
    public String getAllergens(@RequestParam(value = "editId", required = false) Long editId, Model model, HttpSession session) {
        if (isNotAdmin(session)) {
            return "redirect:/login";
        }
        loadAdminModel(model, "allergens");
        Allergen form = (editId != null) ? allergenRepository.findById(editId).orElseGet(Allergen::new) : new Allergen();
        model.addAttribute("allergenForm", form);
        return "admin";
    }

    @PostMapping("/allergens")
    public String postAllergens(@ModelAttribute("allergenForm") Allergen allergen, @RequestParam(value = "deleteId", required = false) Long deleteId, HttpSession session) {
        if (isNotAdmin(session)) {
            return "redirect:/login";
        }
        if (deleteId != null) {
            allergenRepository.deleteById(deleteId);
            return "redirect:/admin/allergens";
        }

        allergenRepository.save(allergen);
        return "redirect:/admin/allergens";
    }
}