package com.cookiecode.cookieapp.config;

import com.cookiecode.cookieapp.model.*;
import com.cookiecode.cookieapp.model.enums.Role;
import com.cookiecode.cookieapp.repository.RecetteRepository;
import com.cookiecode.cookieapp.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PROVISOIRE : quelques recettes pour voir l'accueil et la liste tout de suite.
 * A SUPPRIMER quand le DataSeeder d'AmiraT (8 recettes complètes) est mergé.
 * Ne fait rien si la base contient déjà des recettes.
 */
@Component
public class DevRecetteSeeder implements CommandLineRunner {

    private final RecetteRepository recetteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DevRecetteSeeder(RecetteRepository recetteRepository,
                            UtilisateurRepository utilisateurRepository,
                            PasswordEncoder passwordEncoder) {
        this.recetteRepository = recetteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (recetteRepository.count() > 0) {
            return;
        }

        User chef = utilisateurRepository.findByEmail("chef@cookieapp.test")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("ChefCookie");
                    u.setEmail("chef@cookieapp.test");
                    u.setPassword_hash(passwordEncoder.encode("motdepasse123"));
                    u.setRole(Role.USER);
                    u.setRegistration_date(LocalDate.now());
                    return utilisateurRepository.save(u);
                });

        // du plus ancien au plus récent (minusHours décroissant)
        creer(chef, "Tiramisu", "Le classique italien : mascarpone, café et cacao.",
                Difficulte.MOYEN, 30, 0, true, false, true, 5);
        creer(chef, "Crêpes maison", "Une pâte légère, prête en dix minutes, pour le goûter.",
                Difficulte.FACILE, 10, 20, true, false, false, 4);
        creer(chef, "Brownie vegan", "Fondant au chocolat noir, sans œufs ni beurre.",
                Difficulte.FACILE, 15, 25, true, true, false, 3);
        creer(chef, "Tarte aux pommes", "Pâte brisée maison et fines lamelles de pommes caramélisées.",
                Difficulte.MOYEN, 30, 40, true, false, false, 2);
        creer(chef, "Madeleines", "Petits gâteaux bombés au beurre et au zeste de citron.",
                Difficulte.FACILE, 15, 10, true, false, false, 1);
        creer(chef, "Cookies aux pépites de chocolat", "Croustillants dehors, moelleux dedans.",
                Difficulte.FACILE, 15, 12, true, false, false, 0);
    }

    private void creer(User auteur, String titre, String description,
                       Difficulte difficulte, int prep, int cuisson,
                       boolean vegetarien, boolean vegan, boolean premium, int ageEnHeures) {
        Recipe r = new Recipe();
        r.setTitle(titre);
        r.setDescription(description);
        r.setDifficulty(difficulte);
        r.setPreparationTime(prep);
        r.setCookingTime(cuisson);
        r.setVegetarian(vegetarien);
        r.setVegan(vegan);
        r.setPremium(premium);
        r.setAuthor(auteur);
        r.setCreatedAt(LocalDateTime.now().minusHours(ageEnHeures));
        recetteRepository.save(r);
    }
}
