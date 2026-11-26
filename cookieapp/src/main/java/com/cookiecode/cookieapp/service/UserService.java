package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Allergen;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.Utensil;
import com.cookiecode.cookieapp.model.enums.Role;
import com.cookiecode.cookieapp.repository.AllergenRepository;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import com.cookiecode.cookieapp.repository.UtensilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;
    private final AllergenRepository allergenRepository;
    private final UtensilRepository utensilRepository;
    private final RecipeRepository recipeRepository;

    // RG-U1: unique email and username, confirmed password hashed with BCrypt, FREE subscription
    @Transactional
    public User register(RegisterDTO form) {
        String email = normalizeEmail(form.getEmail());
        String username = form.getUsername().trim();

        if (!form.isPasswordConfirmed()) {
            throw new BusinessException("confirmPassword", "Les deux mots de passe ne sont pas identiques.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("email", "Un compte existe déjà avec cette adresse email.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("username", "Ce pseudo est déjà pris.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword_hash(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);
        user.setRegistration_date(LocalDate.now());

        User saved = userRepository.save(user);
        subscriptionService.createFree(saved);
        return saved;
    }

    // Same message for an unknown email and a wrong password, so nobody can test which emails have an account
    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(normalizeEmail(email))
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword_hash()))
                .orElseThrow(() -> new BusinessException("Email ou mot de passe incorrect."));
    }

    public PublicUser toPublicUser(User user) {
        return new PublicUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                subscriptionService.isPremium(user),
                user.getRole() == Role.ADMIN);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public List<Allergen> allAllergens() {
        return allergenRepository.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public List<Utensil> allUtensils() {
        return utensilRepository.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public Set<Long> allergenIds(User user) {
        return findById(user.getId()).getAllergies().stream()
                .map(Allergen::getId)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<Long> utensilIds(User user) {
        return findById(user.getId()).getUtensils().stream()
                .map(Utensil::getId)
                .collect(Collectors.toSet());
    }

    // N-N: the ticked boxes replace the whole list; no box ticked means no allergy
    @Transactional
    public void updateAllergies(User user, Set<Long> allergenIds) {
        User managed = findById(user.getId());
        managed.getAllergies().clear();
        if (allergenIds != null) {
            managed.getAllergies().addAll(allergenRepository.findAllById(allergenIds));
        }
    }

    // N-N: the ticked boxes replace the whole list; no box ticked means no utensil
    @Transactional
    public void updateUtensils(User user, Set<Long> utensilIds) {
        User managed = findById(user.getId());
        managed.getUtensils().clear();
        if (utensilIds != null) {
            managed.getUtensils().addAll(utensilRepository.findAllById(utensilIds));
        }
    }

    // N-N favorites: a first click adds the recipe, a second click removes it. Returns true if it is now a favorite.
    @Transactional
    public boolean toggleFavorite(User user, Long recipeId) {
        User managed = findById(user.getId());
        boolean removed = managed.getFavorites().removeIf(recipe -> recipe.getId().equals(recipeId));
        if (removed) {
            return false;
        }
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        managed.getFavorites().add(recipe);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(User user, Long recipeId) {
        return findById(user.getId()).getFavorites().stream()
                .anyMatch(recipe -> recipe.getId().equals(recipeId));
    }

    @Transactional(readOnly = true)
    public List<Recipe> favoriteRecipes(User user) {
        return findById(user.getId()).getFavorites().stream()
                .sorted(Comparator.comparing(Recipe::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Recipe> myRecipes(User user) {
        return recipeRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId());
    }

    static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
