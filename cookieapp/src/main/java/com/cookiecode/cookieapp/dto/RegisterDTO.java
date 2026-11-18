package com.cookiecode.cookieapp.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {

    @NotBlank(message = "Choisissez un pseudo.")
    @Size(min = 3, max = 30, message = "Le pseudo doit faire entre 3 et 30 caractères.")
    private String username;

    @NotBlank(message = "L'adresse email est obligatoire.")
    @Email(message = "Adresse email invalide.")
    private String email;

    // BCrypt ignores everything after 72 bytes
    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, max = 72, message = "Le mot de passe doit faire au moins 8 caractères.")
    private String password;

    @NotBlank(message = "Confirmez le mot de passe.")
    private String confirmPassword;

    @AssertTrue(message = "Les deux mots de passe ne sont pas identiques.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
