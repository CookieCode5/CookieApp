package com.cookiecode.cookieapp.model;

public enum Difficulte {
    FACILE("Facile"),
    MOYEN("Moyen"),
    DIFFICILE("Difficile");

    private final String libelle;

    Difficulte(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
