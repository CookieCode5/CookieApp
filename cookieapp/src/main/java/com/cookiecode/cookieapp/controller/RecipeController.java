/*package com.cookiecode.cookieapp.controller;

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

    // Liste des recettes (lecture seule). Recherche et filtres : Sprint 3. 
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
        return "recipes/detail";
    }
}*/
package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.dto.IngredientLineForm;
import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.RecipeForm;
import com.cookiecode.cookieapp.dto.ReviewForm;
import com.cookiecode.cookieapp.dto.StepForm;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.Category;
import com.cookiecode.cookieapp.model.enums.Difficulty;
import com.cookiecode.cookieapp.model.enums.Unit;
import com.cookiecode.cookieapp.repository.IngredientRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import com.cookiecode.cookieapp.repository.UtensilRepository;
import com.cookiecode.cookieapp.service.FileStorageService;
import com.cookiecode.cookieapp.service.RecipeService;
import com.cookiecode.cookieapp.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
public class RecipeController {

    private final RecipeService recipeService;
    private final ReviewService reviewService;
    private final IngredientRepository ingredientRepository;
    private final UtensilRepository utensilRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorage;

    public RecipeController(RecipeService recipeService, ReviewService reviewService,
                            IngredientRepository ingredientRepository, UtensilRepository utensilRepository,
                            UserRepository userRepository, FileStorageService fileStorage) {
        this.recipeService = recipeService;
        this.reviewService = reviewService;
        this.ingredientRepository = ingredientRepository;
        this.utensilRepository = utensilRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
    }

    // ---------------- LIST + SEARCH (task 5) ----------------
    @GetMapping("/recipes")
    public String liste(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Category category,
                        @RequestParam(required = false) Boolean vegetarian,
                        @RequestParam(required = false) Integer maxTime,
                        @RequestParam(required = false, defaultValue = "recent") String sort,
                        Model model) {
        List<Recipe> recipes = recipeService.search(q, category, vegetarian, maxTime, sort);
        model.addAttribute("recettes", recipes);
        model.addAttribute("nbRecettes", recipes.size());
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("vegetarian", vegetarian);
        model.addAttribute("maxTime", maxTime);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", Category.values());
        return "recipes/liste";
    }

    // ---------------- DETAIL ----------------
    @GetMapping("/recipes/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) Integer servings,
                         Model model, HttpSession session) {
        Recipe recipe = recipeService.getById(id);
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        boolean isAuthor = isAuthor(recipe, me);

        model.addAttribute("recipe", recipe);
        model.addAttribute("reviews", reviewService.forRecipe(id));
        model.addAttribute("stars", reviewService.starDistribution(id));
        model.addAttribute("canReview", me != null && !isAuthor);
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("canEdit", me != null && (isAuthor || me.isAdmin()));

        int requested = servings != null ? servings : recipe.getBaseServings();
        model.addAttribute("servings", requested);
        model.addAttribute("recipeCost", recipeService.recipeCost(recipe, requested));
        model.addAttribute("costPerServing", recipeService.costPerServing(recipe));
        model.addAttribute("missingCost", me == null ? BigDecimal.ZERO
                : recipeService.missingCost(recipe, requested, me.getId()));

        ReviewForm form = new ReviewForm();
        if (me != null) {
            reviewService.findMine(me.getId(), id).ifPresent(r -> {
                form.setRating(r.getRating());
                form.setComment(r.getComment());
            });
        }
        model.addAttribute("reviewForm", form);
        return "recipes/detail";
    }

    // ---------------- CREATE ----------------
    @GetMapping("/recipes/new")
    public String newForm(HttpSession session, Model model, RedirectAttributes redirect) {
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        if (me == null) {
            redirect.addFlashAttribute("error", "Connectez-vous pour créer une recette.");
            return "redirect:/login";
        }
        RecipeForm form = new RecipeForm();
        form.getIngredients().add(new IngredientLineForm());
        form.getSteps().add(new StepForm());
        return fillForm(model, form, false);
    }

    @PostMapping("/recipes/new")
    public String create(@ModelAttribute("recipeForm") RecipeForm form,
                         BindingResult result, HttpSession session,
                         Model model, RedirectAttributes redirect) {
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        if (me == null) {
            return "redirect:/login";
        }
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            result.rejectValue("title", "required", "Le titre est obligatoire.");
            return fillForm(model, form, false);
        }
        try {
            String imageUrl = form.getImageUrl();
            String stored = fileStorage.store(form.getImageFile());
            if (stored != null) {
                imageUrl = stored;
            }
            User author = userRepository.getReferenceById(me.getId());
            Recipe saved = recipeService.saveForm(form, author, imageUrl);
            redirect.addFlashAttribute("success", "Recette créée !");
            return "redirect:/recipes/" + saved.getId();
        } catch (BusinessException e) {
            result.reject("business", e.getMessage());
            return fillForm(model, form, false);
        }
    }

    // ---------------- EDIT ----------------
    @GetMapping("/recipes/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session,
                           Model model, RedirectAttributes redirect) {
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        Recipe recipe = recipeService.getById(id);
        if (!canManageRecipe(recipe, me)) {
            redirect.addFlashAttribute("error", "Vous ne pouvez modifier que vos propres recettes.");
            return "redirect:/recipes/" + id;
        }
        return fillForm(model, recipeService.toForm(recipe), true);
    }

    @PostMapping("/recipes/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("recipeForm") RecipeForm form,
                         BindingResult result, HttpSession session,
                         Model model, RedirectAttributes redirect) {
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        Recipe recipe = recipeService.getById(id);
        if (!canManageRecipe(recipe, me)) {
            redirect.addFlashAttribute("error", "Vous ne pouvez modifier que vos propres recettes.");
            return "redirect:/recipes/" + id;
        }
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            result.rejectValue("title", "required", "Le titre est obligatoire.");
            form.setId(id);
            return fillForm(model, form, true);
        }
        try {
            String imageUrl = form.getImageUrl();
            String stored = fileStorage.store(form.getImageFile());
            if (stored != null) {
                imageUrl = stored;
            }
            form.setId(id);
            User author = userRepository.getReferenceById(recipe.getAuthor().getId());
            recipeService.saveForm(form, author, imageUrl);
            redirect.addFlashAttribute("success", "Recette mise à jour.");
            return "redirect:/recipes/" + id;
        } catch (BusinessException e) {
            result.reject("business", e.getMessage());
            form.setId(id);
            return fillForm(model, form, true);
        }
    }

    // ---------------- DELETE ----------------
    @PostMapping("/recipes/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        Recipe recipe = recipeService.getById(id);
        if (!canManageRecipe(recipe, me)) {
            redirect.addFlashAttribute("error", "Vous ne pouvez supprimer que vos propres recettes.");
            return "redirect:/recipes/" + id;
        }
        recipeService.deleteById(id);
        redirect.addFlashAttribute("success", "Recette supprimée.");
        return "redirect:/recipes";
    }

    private boolean isAuthor(Recipe recipe, PublicUser me) {
        return me != null && recipe.getAuthor() != null
                && Objects.equals(recipe.getAuthor().getId(), me.getId());
    }

    private boolean canManageRecipe(Recipe recipe, PublicUser me) {
        return me != null && (me.isAdmin() || isAuthor(recipe, me));
    }

    private String fillForm(Model model, RecipeForm form, boolean edition) {
        model.addAttribute("recipeForm", form);
        model.addAttribute("edition", edition);
        model.addAttribute("categories", Category.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("units", Unit.values());
        model.addAttribute("ingredients", ingredientRepository.findAll());
        model.addAttribute("utensils", utensilRepository.findAll());
        return "recipes/form";
    }
}
