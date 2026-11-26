package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.Subscription;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.Role;
import com.cookiecode.cookieapp.model.enums.SubscriptionType;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Real services and real H2 database; @Transactional rolls every test back
@SpringBootTest
@Transactional
class SubscriptionServiceTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    private User registerChef() {
        RegisterDTO form = new RegisterDTO();
        form.setUsername("chef");
        form.setEmail("chef@example.com");
        form.setPassword("password123");
        form.setConfirmPassword("password123");
        return userService.register(form);
    }

    @Test
    void aNewAccountIsFree() {
        User user = registerChef();

        assertFalse(subscriptionService.isPremium(user));
    }

    @Test
    void upgradeToPremium_givesOneMonthOfPremium() {
        User user = registerChef();

        Subscription subscription = subscriptionService.upgradeToPremium(user, 1);

        assertEquals(SubscriptionType.PREMIUM, subscription.getSubType());
        assertEquals(LocalDate.now().plusMonths(1), subscription.getEnd_date());
        assertTrue(subscriptionService.isPremium(user));
    }

    @Test
    void upgradeToPremium_refusesAnAccountThatIsAlreadyPremium() {
        User user = registerChef();
        subscriptionService.upgradeToPremium(user, 1);

        assertThrows(BusinessException.class, () -> subscriptionService.upgradeToPremium(user, 1));
    }

    @Test
    void upgradeToPremium_createsTheSubscriptionWhenTheAccountHasNone() {
        User user = new User();
        user.setUsername("noSubscription");
        user.setEmail("no-subscription@example.com");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        subscriptionService.upgradeToPremium(user, 1);

        assertTrue(subscriptionService.isPremium(user));
    }

    private Recipe newRecipe(boolean premium, User author) {
        Recipe recipe = new Recipe();
        recipe.setTitle(premium ? "Premium tart" : "Simple cookies");
        recipe.setPremium(premium);
        recipe.setAuthor(author);
        return recipeRepository.save(recipe);
    }

    private User otherAuthor() {
        User author = new User();
        author.setUsername("pastryChef");
        author.setEmail("pastry-chef@example.com");
        author.setRole(Role.USER);
        return userRepository.save(author);
    }

    @Test
    void canView_aNormalRecipeIsOpenToEveryone() {
        Recipe recipe = newRecipe(false, otherAuthor());

        assertTrue(subscriptionService.canView(null, recipe));
        assertTrue(subscriptionService.canView(registerChef(), recipe));
    }

    @Test
    void canView_aPremiumRecipeIsClosedToVisitorsAndFreeAccounts() {
        Recipe recipe = newRecipe(true, otherAuthor());

        assertFalse(subscriptionService.canView(null, recipe));
        assertFalse(subscriptionService.canView(registerChef(), recipe));
    }

    @Test
    void canView_aPremiumRecipeOpensOncePremium() {
        Recipe recipe = newRecipe(true, otherAuthor());
        User user = registerChef();

        subscriptionService.upgradeToPremium(user, 1);

        assertTrue(subscriptionService.canView(user, recipe));
    }

    @Test
    void canView_theAuthorAlwaysSeesTheirPremiumRecipe() {
        User author = registerChef();
        Recipe recipe = newRecipe(true, author);

        assertTrue(subscriptionService.canView(author, recipe));
    }
}
