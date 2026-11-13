package com.cookiecode.cookieapp.model;

public enum Categorie {
    ENTREE("Entrée"),
    PLAT("Plat"),
    DESSERT("Dessert"),
    PATISSERIE("Pâtisserie"),
    GOUTER("Goûter");

    private final String libelle;

    Categorie(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
