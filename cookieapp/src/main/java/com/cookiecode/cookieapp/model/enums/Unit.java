package com.cookiecode.cookieapp.model.enums;

public enum Unit {
    G(1), KG(1000), ML(1), CL(10), L(1000), PIECE(1), TEASPOON(5), TABLESPOON(15);

    private final double factor;
    Unit(double factor) { this.factor = factor; }

    public double toBase(double quantity) { return quantity * factor; }
}