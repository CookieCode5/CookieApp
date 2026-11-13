package com.cookiecode.cookieapp.config;

import com.cookiecode.cookieapp.model.*;
import com.cookiecode.cookieapp.repository.RecetteRepository;
import com.cookiecode.cookieapp.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

        Utilisateur chef = utilisateurRepository.findByEmail("chef@cookieapp.test")
                .orElseGet(() -> {
                    Utilisateur u = new Utilisateur();
                    u.setPseudo("ChefCookie");
                    u.setEmail("chef@cookieapp.test");
                    u.setMotDePasseHash(passwordEncoder.encode("motdepasse123"));
                    return utilisateurRepository.save(u);
                });

        // du plus ancien au plus récent (minusHours décroissant)
        creer(chef, "Tiramisu", "Le classique italien : mascarpone, café et cacao.",
                Categorie.DESSERT, Difficulte.MOYEN, 30, 0, true, false, true, 5);
        creer(chef, "Crêpes maison", "Une pâte légère, prête en dix minutes, pour le goûter.",
                Categorie.GOUTER, Difficulte.FACILE, 10, 20, true, false, false, 4);
        creer(chef, "Salade méchouia", "Poivrons et tomates grillés, ail et huile d'olive.",
                Categorie.ENTREE, Difficulte.FACILE, 15, 15, true, true, false, 3);
        creer(chef, "Ratatouille", "Légumes d'été mijotés doucement, sans produit animal.",
                Categorie.PLAT, Difficulte.FACILE, 20, 40, true, true, false, 2);
        creer(chef, "Spaghetti carbonara", "Œufs, pecorino et guanciale : pas de crème !",
                Categorie.PLAT, Difficulte.FACILE, 10, 15, false, false, false, 1);
        creer(chef, "Cookies aux pépites de chocolat", "Croustillants dehors, moelleux dedans.",
                Categorie.PATISSERIE, Difficulte.FACILE, 15, 12, true, false, false, 0);
    }

    private void creer(Utilisateur auteur, String titre, String description, Categorie categorie,
                       Difficulte difficulte, int prep, int cuisson,
                       boolean vegetarien, boolean vegan, boolean premium, int ageEnHeures) {
        Recette r = new Recette();
        r.setTitre(titre);
        r.setDescription(description);
        r.setCategorie(categorie);
        r.setDifficulte(difficulte);
        r.setTempsPreparation(prep);
        r.setTempsCuisson(cuisson);
        r.setVegetarien(vegetarien);
        r.setVegan(vegan);
        r.setPremium(premium);
        r.setAuteur(auteur);
        r.setDateCreation(LocalDateTime.now().minusHours(ageEnHeures));
        recetteRepository.save(r);
    }
}
