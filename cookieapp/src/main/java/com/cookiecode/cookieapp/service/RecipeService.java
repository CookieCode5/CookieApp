package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.IngredientLineForm;
import com.cookiecode.cookieapp.dto.RecipeDTO;
import com.cookiecode.cookieapp.dto.StepForm;
import com.cookiecode.cookieapp.model.Ingredient;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.RecipeIngredient;
import com.cookiecode.cookieapp.model.Step;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.Utensil;
import com.cookiecode.cookieapp.repository.IngredientRepository;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import com.cookiecode.cookieapp.repository.UtensilRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository RecipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final UtensilRepository utensilRepository;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, IngredientRepository ingredientRepository, UtensilRepository utensilRepository) {
    this.RecipeRepository = recipeRepository;
    this.userRepository = userRepository;
    this.ingredientRepository = ingredientRepository;
    this.utensilRepository = utensilRepository;
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


    @Transactional(readOnly = true)
    public Recipe findById(Long id) {
        return RecipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }

    @Transactional
    public Recipe createFromDto(RecipeDTO dto) {
        User author = userRepository.findById(dto.getAuthorId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        Recipe recipe = new Recipe();
        applyDtoFields(recipe, dto, author);
        return RecipeRepository.save(recipe);
    }

@Transactional
public Recipe updateFromDto(Long id, RecipeDTO dto) {
    Recipe recipe = findById(id); // lève 404 si introuvable
    User author = userRepository.findById(dto.getAuthorId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));

    // Réinitialise les collections gérées en 1-N (avec orphanRemoval = true sur Recipe)
    recipe.getIngredients().clear();
    recipe.getSteps().clear();
    recipe.getUtensils().clear();

    applyDtoFields(recipe, dto, author);
    return RecipeRepository.save(recipe);
}

@Transactional
public void deleteById(Long id) {
    Recipe recipe = findById(id); // lève 404 si introuvable
    RecipeRepository.delete(recipe);
}

private void applyDtoFields(Recipe recipe, RecipeDTO dto, User author) {
    recipe.setAuthor(author);
    recipe.setTitle(dto.getTitle());
    recipe.setDescription(dto.getDescription());

    if (dto.getUtensilIds() != null && !dto.getUtensilIds().isEmpty()) {
        Set<Utensil> utensils = new HashSet<>(utensilRepository.findAllById(dto.getUtensilIds()));
        recipe.setUtensils(utensils);
    }

    if (dto.getIngredients() != null) {
        for (IngredientLineForm line : dto.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(line.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Ingredient not found: " + line.getIngredientId()));

            RecipeIngredient ri = new RecipeIngredient();
            ri.setRecipe(recipe);
            ri.setIngredient(ingredient);
            ri.setQuantity(line.getQuantity());
            ri.setUnit(line.getUnit());
            ri.setOptional(line.isOptional());
            recipe.getIngredients().add(ri);
        }
    }

    if (dto.getSteps() != null) {
        for (StepForm stepForm : dto.getSteps()) {
            Step step = new Step();
            step.setRecipe(recipe);
            step.setStepOrder(stepForm.getStepOrder());
            step.setTitle(stepForm.getTitle());
            step.setInstruction(stepForm.getInstruction());
            step.setDurationMinutes(stepForm.getDurationMinutes());
            step.setTip(stepForm.getTip());
            step.setParallelizable(stepForm.isParallelizable());
            recipe.getSteps().add(step);
        }
    }
}




}