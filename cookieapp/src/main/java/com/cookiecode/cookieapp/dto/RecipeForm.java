package com.cookiecode.cookieapp.dto;

import com.cookiecode.cookieapp.model.enums.Category;
import com.cookiecode.cookieapp.model.enums.Difficulty;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeForm {

    private Long id;
    private String title;
    private String description;
    private Category category;
    private Difficulty difficulty;
    private int baseServings = 4;
    private int preparationTime;
    private int cookingTime;
    private boolean vegetarian;
    private boolean vegan;
    private boolean premium;
    private String imageUrl;
    private MultipartFile imageFile;
    private Set<Long> utensilIds = new HashSet<>();
    private List<IngredientLineForm> ingredients = new ArrayList<>();
    private List<StepForm> steps = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public int getBaseServings() { return baseServings; }
    public void setBaseServings(int baseServings) { this.baseServings = baseServings; }
    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }
    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public boolean isVegetarian() { return vegetarian; }
    public void setVegetarian(boolean vegetarian) { this.vegetarian = vegetarian; }
    public boolean isVegan() { return vegan; }
    public void setVegan(boolean vegan) { this.vegan = vegan; }
    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
    public Set<Long> getUtensilIds() { return utensilIds; }
    public void setUtensilIds(Set<Long> utensilIds) { this.utensilIds = utensilIds; }
    public List<IngredientLineForm> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientLineForm> ingredients) { this.ingredients = ingredients; }
    public List<StepForm> getSteps() { return steps; }
    public void setSteps(List<StepForm> steps) { this.steps = steps; }
}