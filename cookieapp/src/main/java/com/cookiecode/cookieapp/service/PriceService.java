package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.model.Ingredient;
import com.cookiecode.cookieapp.model.PantryItem;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.RecipeIngredient;
import com.cookiecode.cookieapp.model.enums.Unit;
import com.cookiecode.cookieapp.repository.PantryItemRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component("price")
public class PriceService {

    private final PantryItemRepository pantryItemRepository;

    public PriceService(PantryItemRepository pantryItemRepository) {
        this.pantryItemRepository = pantryItemRepository;
    }

    public BigDecimal recipeCost(Recipe recipe, int servings) {
        if (recipe == null || recipe.getIngredients() == null) return BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (RecipeIngredient line : recipe.getIngredients()) {
            total = total.add(lineCost(line, servings));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal costPerServing(Recipe recipe) {
        if (recipe == null) return BigDecimal.ZERO;
        int s = Math.max(1, recipe.getBaseServings());
        return recipeCost(recipe, s).divide(BigDecimal.valueOf(s), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal missingCost(Recipe recipe, int servings, Long userId) {
        if (recipe == null || recipe.getIngredients() == null) return BigDecimal.ZERO;
        List<PantryItem> pantry = pantryItemRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId() == userId)
                .toList();
        BigDecimal total = BigDecimal.ZERO;
        for (RecipeIngredient line : recipe.getIngredients()) {
            boolean inPantry = pantry.stream().anyMatch(p ->
                    p.getIngredient() != null
                    && line.getIngredient() != null
                    && p.getIngredient().getId().equals(line.getIngredient().getId())
                    && p.getQuantity() >= line.quantityFor(servings));
            if (!inPantry) total = total.add(lineCost(line, servings));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal lineCost(RecipeIngredient line, int servings) {
        if (line == null) return BigDecimal.ZERO;
        Ingredient ing = line.getIngredient();
        if (ing == null || ing.getPricePerUnit() == null) return BigDecimal.ZERO;
        
        double qty = line.quantityFor(servings);
        if (qty <= 0) return BigDecimal.ZERO;

        Unit lineUnit = line.getUnit();
        Unit refUnit = ing.getReferenceUnit();
        
        // Si l'unité manque, on suppose que la quantité est déjà à l'unité de base
        if (lineUnit == null) {
            return ing.getPricePerUnit().multiply(BigDecimal.valueOf(qty));
        }

        double baseQty = toBaseCompatible(qty, lineUnit, refUnit);
        return ing.getPricePerUnit().multiply(BigDecimal.valueOf(baseQty));
    }

    private double toBaseCompatible(double qty, Unit lineUnit, Unit refUnit) {
        if (refUnit == null) return lineUnit.toBase(qty);
        if (sameFamily(lineUnit, refUnit)) return lineUnit.toBase(qty);
        return qty; // fallback silencieux
    }

    private boolean sameFamily(Unit a, Unit b) {
        return family(a).equals(family(b));
    }

    private String family(Unit u) {
        return switch (u) {
            case G, KG -> "mass";
            case ML, CL, L -> "volume";
            case PIECE, TEASPOON, TABLESPOON -> "piece";
        };
    }
}