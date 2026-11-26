/*package com.cookiecode.cookieapp.dto;

import com.cookiecode.cookieapp.model.enums.Unit;

public class IngredientLineForm {

    private Long ingredientId;
    private double quantity;
    private Unit unit;
    private boolean optional;

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
}
*/
package com.cookiecode.cookieapp.dto;

import com.cookiecode.cookieapp.model.enums.Unit;

public class IngredientLineForm {

    private Long ingredientId;
    private double quantity;
    private Unit unit;
    private boolean optional;
    private String note;

    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public boolean isOptional() { return optional; }
    public void setOptional(boolean optional) { this.optional = optional; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}