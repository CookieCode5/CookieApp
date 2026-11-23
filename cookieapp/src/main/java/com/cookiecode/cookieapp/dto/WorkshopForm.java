package com.cookiecode.cookieapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WorkshopForm {

    @NotNull(message = "Choisissez une recette.")
    private Long recipeId;

    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 100, message = "100 caractères maximum.")
    private String title;
}