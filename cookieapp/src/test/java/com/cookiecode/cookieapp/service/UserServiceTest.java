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
import com.cookiecode.cookieapp.repository.SubscriptionRepository;
import com.cookiecode.cookieapp.repository.UtensilRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Real service and real H2 database; @Transactional rolls every test back, so nothing stays in the database
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AllergenRepository allergenRepository;

    @Autowired
    private UtensilRepository utensilRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    private static RegisterDTO form(String username, String email, String password, String confirm) {
        RegisterDTO form = new RegisterDTO();
        form.setUsername(username);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(confirm);
        return form;
    }

    private User registerChef() {
        return userService.register(form("chef", "chef@example.com", "password123", "password123"));
    }

    @Test
    void register_createsAUserWithAHashedPasswordAndAFreeSubscription() {
        User user = userService.register(form(" chef ", " Chef@Example.com ", "password123", "password123"));

        assertEquals("chef", user.getUsername());
        assertEquals("chef@example.com", user.getEmail());
        assertEquals(Role.USER, user.getRole());
        assertNotEquals("password123", user.getPassword_hash());
        assertTrue(subscriptionRepository.findByUser(user).isPresent());
        assertFalse(subscriptionService.isPremium(user));
    }

    @Test
    void register_refusesAnEmailAlreadyUsed() {
        registerChef();

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("otherChef", "chef@example.com", "password123", "password123")));

        assertEquals("email", e.getField());
    }

    @Test
    void register_refusesAUsernameAlreadyUsed() {
        registerChef();

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("chef", "other@example.com", "password123", "password123")));

        assertEquals("username", e.getField());
    }

    @Test
    void register_refusesDifferentPasswords() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("chef", "chef@example.com", "password123", "differentPassword")));

        assertEquals("confirmPassword", e.getField());
    }

    @Test
    void authenticate_acceptsTheRightPasswordWhateverTheEmailCase() {
        registerChef();

        User user = userService.authenticate(" Chef@Example.com", "password123");

        assertEquals("chef", user.getUsername());
    }

    @Test
    void authenticate_refusesAWrongPassword() {
        registerChef();

        assertThrows(BusinessException.class, () -> userService.authenticate("chef@example.com", "wrongPassword"));
    }

    @Test
    void authenticate_refusesAnUnknownEmail() {
        assertThrows(BusinessException.class, () -> userService.authenticate("unknown@example.com", "password123"));
    }

    @Test
    void toPublicUser_keepsNoPassword() {
        User user = registerChef();

        PublicUser publicUser = userService.toPublicUser(user);

        assertEquals(user.getId(), publicUser.getId());
        assertEquals("chef", publicUser.getUsername());
        assertFalse(publicUser.isPremium());
        assertFalse(publicUser.isAdmin());
    }

    private Allergen newAllergen(String name) {
        Allergen allergen = new Allergen();
        allergen.setName(name);
        return allergenRepository.save(allergen);
    }

    @Test
    void updateAllergies_replacesTheWholeList() {
        User user = registerChef();
        Allergen sesame = newAllergen("Sesame");
        Allergen mustard = newAllergen("Mustard");

        userService.updateAllergies(user, Set.of(sesame.getId()));
        userService.updateAllergies(user, Set.of(mustard.getId()));

        assertEquals(Set.of(mustard.getId()), userService.allergenIds(user));
    }

    @Test
    void updateAllergies_withNoBoxTickedRemovesEveryAllergy() {
        User user = registerChef();
        userService.updateAllergies(user, Set.of(newAllergen("Sesame").getId()));

        userService.updateAllergies(user, null);

        assertTrue(userService.allergenIds(user).isEmpty());
    }

    @Test
    void updateUtensils_replacesTheWholeList() {
        User user = registerChef();
        Utensil rollingPin = new Utensil();
        rollingPin.setName("Rolling pin");
        rollingPin = utensilRepository.save(rollingPin);

        userService.updateUtensils(user, Set.of(rollingPin.getId()));

        assertEquals(Set.of(rollingPin.getId()), userService.utensilIds(user));
    }

    private Recipe newRecipe(String title, User author) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setAuthor(author);
        return recipeRepository.save(recipe);
    }

    @Test
    void toggleFavorite_addsThenRemovesTheRecipe() {
        User user = registerChef();
        Recipe recipe = newRecipe("Lemon tart", null);

        assertTrue(userService.toggleFavorite(user, recipe.getId()));
        assertTrue(userService.isFavorite(user, recipe.getId()));
        assertEquals(1, userService.favoriteRecipes(user).size());

        assertFalse(userService.toggleFavorite(user, recipe.getId()));
        assertFalse(userService.isFavorite(user, recipe.getId()));
        assertTrue(userService.favoriteRecipes(user).isEmpty());
    }

    @Test
    void myRecipes_onlyListsTheRecipesIWrote() {
        User user = registerChef();
        newRecipe("My brownie", user);
        newRecipe("Somebody else's cake", null);

        assertEquals(1, userService.myRecipes(user).size());
        assertEquals("My brownie", userService.myRecipes(user).get(0).getTitle());
    }
}
