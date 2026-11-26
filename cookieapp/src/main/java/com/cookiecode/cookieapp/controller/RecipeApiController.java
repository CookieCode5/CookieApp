package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.dto.IngredientLineForm;
import com.cookiecode.cookieapp.dto.RecipeDTO;
import com.cookiecode.cookieapp.dto.StepForm;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.RecipeIngredient;
import com.cookiecode.cookieapp.model.Step;
import com.cookiecode.cookieapp.model.Utensil;
import com.cookiecode.cookieapp.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeApiController {

    private final RecipeService recipeService;

    public RecipeApiController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> findAll() {
        List<RecipeDTO> dtos = recipeService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> findById(@PathVariable Long id) {
        Recipe recipe = recipeService.findById(id); // 404 if not found
        return ResponseEntity.ok(toDto(recipe));
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> create(@Valid @RequestBody RecipeDTO dto) {
        Recipe created = recipeService.createFromDto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created)); // 201
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> update(@PathVariable Long id,
                                            @Valid @RequestBody RecipeDTO dto) {
        Recipe updated = recipeService.updateFromDto(id, dto); // 200 (or 404 if not found)
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeService.deleteById(id); // 204 (or 404 if not found)
        return ResponseEntity.noContent().build();
    }

    private RecipeDTO toDto(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());

        if (recipe.getAuthor() != null) {
            dto.setAuthorId(recipe.getAuthor().getId());
        }

        if (recipe.getUtensils() != null) {
            dto.setUtensilIds(recipe.getUtensils().stream()
                    .map(Utensil::getId)
                    .collect(Collectors.toSet()));
        }

        if (recipe.getIngredients() != null) {
            List<IngredientLineForm> lines = recipe.getIngredients().stream()
                    .map(this::toIngredientLineForm)
                    .toList();
            dto.setIngredients(lines);
        }

        if (recipe.getSteps() != null) {
            List<StepForm> stepList = recipe.getSteps().stream()
                    .map(this::toStepForm)
                    .toList();
            dto.setSteps(stepList);
        }

        return dto;
    }

    private IngredientLineForm toIngredientLineForm(RecipeIngredient ri) {
        IngredientLineForm form = new IngredientLineForm();
        if (ri.getIngredient() != null) {
            form.setIngredientId(ri.getIngredient().getId());
        }
        form.setQuantity(ri.getQuantity());
        form.setUnit(ri.getUnit());
        form.setOptional(ri.isOptional());
        return form;
    }

    private StepForm toStepForm(Step step) {
        StepForm form = new StepForm();
        form.setStepOrder(step.getStepOrder());
        form.setTitle(step.getTitle());
        form.setInstruction(step.getInstruction());
        form.setDurationMinutes(step.getDurationMinutes());
        form.setTip(step.getTip());
        form.setParallelizable(step.isParallelizable());
        return form;
    }
}