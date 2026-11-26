package com.cookiecode.cookieapp.model.enums;

public enum Category {
    CAKE("Cake"),
    TART("Tarte"),
    COOKIE("Cookie"),
    BREAD("Pain"),
    PASTRY("Pâtisserie"),
    OTHER("Autre");

    private final String libelle;

    Category(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}