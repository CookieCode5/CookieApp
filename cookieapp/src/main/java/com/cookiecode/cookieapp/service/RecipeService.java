package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.IngredientLineForm;
import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.RecipeDTO;
import com.cookiecode.cookieapp.dto.RecipeForm;
import com.cookiecode.cookieapp.dto.StepForm;
import com.cookiecode.cookieapp.model.Ingredient;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.RecipeIngredient;
import com.cookiecode.cookieapp.model.Step;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.Utensil;
import com.cookiecode.cookieapp.model.enums.Category;
import com.cookiecode.cookieapp.repository.IngredientRepository;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import com.cookiecode.cookieapp.repository.UtensilRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RecipeService {

    private static final int EXPRESS_MAX_MINUTES = 20;
    private static final BigDecimal BUDGET_MAX_PER_SERVING = new BigDecimal("3");

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final UtensilRepository utensilRepository;
    private final PriceService priceService;

    public RecipeService(RecipeRepository recipeRepository,
                         UserRepository userRepository,
                         IngredientRepository ingredientRepository,
                         UtensilRepository utensilRepository,
                         PriceService priceService) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
        this.utensilRepository = utensilRepository;
        this.priceService = priceService;
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    public List<Recipe> findAll() {
        return recipeRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Recipe> latest(int n, String filter) {
        String f = filter == null ? "" : filter;
        return recipeRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> switch (f) {
                    case "vegetarian" -> r.isVegetarian();
                    case "express" -> r.totalTime() <= EXPRESS_MAX_MINUTES;
                    case "budget" -> priceService.costPerServing(r).compareTo(BUDGET_MAX_PER_SERVING) <= 0;
                    default -> true;
                })
                .limit(n)
                .toList();
    }

    public long count() {
        return recipeRepository.count();
    }

    public Recipe getById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }

    public Recipe findById(Long id) {
        return getById(id);
    }

    /** Feature 5 — search by title + filters (category / vegetarian / maxTime) + sort. */
    public List<Recipe> search(String q, Category category, Boolean vegetarian, Integer maxTime, String sort) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        String order = (sort == null || sort.isBlank()) ? "recent" : sort;
        return recipeRepository.search(query, category, vegetarian, maxTime, order);
    }

    // ------------------------------------------------------------------
    // PRICE (Feature 4) — thin delegates, so the controller and Thymeleaf
    // can call either the service or the @price bean.
    // ------------------------------------------------------------------

    public BigDecimal recipeCost(Recipe r, int servings) {
        return priceService.recipeCost(r, servings);
    }

    public BigDecimal costPerServing(Recipe r) {
        return priceService.costPerServing(r);
    }

    public BigDecimal missingCost(Recipe r, int servings, Long userId) {
        return priceService.missingCost(r, servings, userId);
    }

    // ------------------------------------------------------------------
    // WRITE — DTO path (API + internal)
    // ------------------------------------------------------------------

    @Transactional
    public Recipe createFromDto(RecipeDTO dto) {
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        Recipe recipe = new Recipe();
        applyDtoFields(recipe, dto, author);
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe updateFromDto(Long id, RecipeDTO dto) {
        Recipe recipe = findById(id);
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        recipe.getIngredients().clear();
        recipe.getSteps().clear();
        recipe.getUtensils().clear();
        applyDtoFields(recipe, dto, author);
        return recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteById(Long id) {
        Recipe recipe = findById(id);
        // A recipe that someone saved as a favorite must first leave their favorites (N-N join table)
        recipe.getUsers().forEach(user -> user.getFavorites().remove(recipe));
        recipeRepository.delete(recipe);
    }

    private void applyDtoFields(Recipe recipe, RecipeDTO dto, User author) {
        recipe.setAuthor(author);
        recipe.setTitle(dto.getTitle());
        recipe.setDescription(dto.getDescription());

        recipe.setCategory(dto.getCategory());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setBaseServings(dto.getBaseServings());
        recipe.setPreparationTime(dto.getPreparationTime());
        recipe.setCookingTime(dto.getCookingTime());
        recipe.setVegetarian(dto.isVegetarian());
        recipe.setVegan(dto.isVegan());
        recipe.setPremium(dto.isPremium());
        recipe.setImageUrl(dto.getImageUrl());

        if (dto.getUtensilIds() != null && !dto.getUtensilIds().isEmpty()) {
            Set<Utensil> utensils = new HashSet<>(utensilRepository.findAllById(dto.getUtensilIds()));
            recipe.setUtensils(utensils);
        }

        if (dto.getIngredients() != null) {
            for (IngredientLineForm line : dto.getIngredients()) {
                if (line.getIngredientId() == null) continue;
                Ingredient ingredient = ingredientRepository.findById(line.getIngredientId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Ingredient not found: " + line.getIngredientId()));

                RecipeIngredient ri = new RecipeIngredient();
                ri.setRecipe(recipe);
                ri.setIngredient(ingredient);
                ri.setQuantity(line.getQuantity());
                ri.setUnit(line.getUnit());
                ri.setOptional(line.isOptional());
                ri.setNote(line.getNote());
                recipe.getIngredients().add(ri);
            }
        }

        if (dto.getSteps() != null) {
            int order = 1;
            for (StepForm stepForm : dto.getSteps()) {
                if (stepForm.getTitle() == null && stepForm.getInstruction() == null) continue;
                Step step = new Step();
                step.setRecipe(recipe);
                step.setStepOrder(order++);
                step.setTitle(stepForm.getTitle());
                step.setInstruction(stepForm.getInstruction());
                step.setDurationMinutes(stepForm.getDurationMinutes());
                step.setTip(stepForm.getTip());
                step.setParallelizable(stepForm.isParallelizable());
                recipe.getSteps().add(step);
            }
        }
    }

    // ------------------------------------------------------------------
    // WRITE — form path (web: create + edit, Feature 1 / 2 / 3)
    // ------------------------------------------------------------------

    /**
     * Save a web RecipeForm as a new Recipe (form.id == null) or update an existing one.
     * imageUrl is resolved by the controller (uploaded file -> /uploads/..., or fallback URL).
     */
    @Transactional
    public Recipe saveForm(RecipeForm form, User author, String imageUrl) {
        RecipeDTO dto = new RecipeDTO();
        dto.setTitle(form.getTitle());
        dto.setDescription(form.getDescription());
        dto.setAuthorId(author.getId());
        dto.setCategory(form.getCategory());
        dto.setDifficulty(form.getDifficulty());
        dto.setBaseServings(form.getBaseServings());
        dto.setPreparationTime(form.getPreparationTime());
        dto.setCookingTime(form.getCookingTime());
        dto.setVegetarian(form.isVegetarian());
        dto.setVegan(form.isVegan());
        dto.setPremium(form.isPremium());
        dto.setImageUrl(imageUrl);
        dto.setUtensilIds(form.getUtensilIds());
        dto.setIngredients(form.getIngredients());
        dto.setSteps(form.getSteps());

        if (form.getId() != null) {
            Recipe recipe = findById(form.getId());
            recipe.getIngredients().clear();
            recipe.getSteps().clear();
            recipe.getUtensils().clear();
            applyDtoFields(recipe, dto, author);
            return recipeRepository.save(recipe);
        }
        Recipe recipe = new Recipe();
        applyDtoFields(recipe, dto, author);
        return recipeRepository.save(recipe);
    }

    /** Pre-fill a RecipeForm from an existing Recipe (edit page). */
    public RecipeForm toForm(Recipe recipe) {
        RecipeForm form = new RecipeForm();
        form.setId(recipe.getId());
        form.setTitle(recipe.getTitle());
        form.setDescription(recipe.getDescription());
        form.setCategory(recipe.getCategory());
        form.setDifficulty(recipe.getDifficulty());
        form.setBaseServings(recipe.getBaseServings());
        form.setPreparationTime(recipe.getPreparationTime());
        form.setCookingTime(recipe.getCookingTime());
        form.setVegetarian(recipe.isVegetarian());
        form.setVegan(recipe.isVegan());
        form.setPremium(recipe.isPremium());
        form.setImageUrl(recipe.getImageUrl());

        Set<Long> utensils = new HashSet<>();
        recipe.getUtensils().forEach(u -> utensils.add(u.getId()));
        form.setUtensilIds(utensils);

        for (RecipeIngredient ri : recipe.getIngredients()) {
            IngredientLineForm line = new IngredientLineForm();
            if (ri.getIngredient() != null) {
                line.setIngredientId(ri.getIngredient().getId());
            }
            line.setQuantity(ri.getQuantity());
            line.setUnit(ri.getUnit());
            line.setOptional(ri.isOptional());
            line.setNote(ri.getNote());
            form.getIngredients().add(line);
        }

        for (Step s : recipe.getSteps()) {
            StepForm sf = new StepForm();
            sf.setStepOrder(s.getStepOrder());
            sf.setTitle(s.getTitle());
            sf.setInstruction(s.getInstruction());
            sf.setDurationMinutes(s.getDurationMinutes());
            sf.setTip(s.getTip());
            sf.setParallelizable(s.isParallelizable());
            form.getSteps().add(sf);
        }
        return form;
    }

    // ------------------------------------------------------------------
    // AUTHORIZATION helper (Feature 3 — author or ADMIN only)
    // ------------------------------------------------------------------

    public boolean canEdit(Recipe recipe, PublicUser me) {
        if (me == null || recipe == null) return false;
        if (me.isAdmin()) return true;
        return recipe.getAuthor() != null && Objects.equals(recipe.getAuthor().getId(), me.getId());
    }
}