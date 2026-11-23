package com.cookiecode.cookieapp.model;

import com.cookiecode.cookieapp.model.enums.Unit;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
public class RecipeIngredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private double quantity;
    @Enumerated(EnumType.STRING) private Unit unit;
    private boolean optional;

    public double quantityFor(int servings) {
        return quantity * servings / recipe.getBaseServings();
    }
}
