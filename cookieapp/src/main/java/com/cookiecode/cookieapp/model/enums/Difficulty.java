package com.cookiecode.cookieapp.model.enums;

public enum Difficulty {
    EASY("Facile"), MEDIUM("Moyen"), HARD("Difficile");

    private final String libelle;
    Difficulty(String libelle) { this.libelle = libelle; }
    public String getLibelle() { return libelle; }
}