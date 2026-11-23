package com.cookiecode.cookieapp.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RecipeDTO {

    private Long id;
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull (message = "L'identifiant de l'auteur (authorId) est obligatoire")
    private Long authorId;

    private Set<Long> utensilIds = new HashSet<>();

    @Valid
    private List<IngredientLineForm> ingredients = new ArrayList<>();

    @Valid
    private List<StepForm> steps = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Set<Long> getUtensilIds() {
        return utensilIds;
    }

    public void setUtensilIds(Set<Long> utensilIds) {
        this.utensilIds = utensilIds;
    }

    public List<IngredientLineForm> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientLineForm> ingredients) {
        this.ingredients = ingredients;
    }

    public List<StepForm> getSteps() {
        return steps;
    }

    public void setSteps(List<StepForm> steps) {
        this.steps = steps;
    }
}