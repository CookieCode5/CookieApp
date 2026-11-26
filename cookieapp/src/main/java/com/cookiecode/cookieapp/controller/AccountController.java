package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Subscription;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.service.SubscriptionService;
import com.cookiecode.cookieapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

// Every route here is protected by LoginInterceptor (/account/**, /offers/** and /recipes/*/favorite in WebConfig)
@Controller
@RequiredArgsConstructor
public class AccountController {

    private static final int PREMIUM_MONTHS = 1;

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/account")
    public String account(Model model, HttpSession session) {
        User user = currentUser(session);
        addProfile(model, user, "preferences");
        model.addAttribute("allergens", userService.allAllergens());
        model.addAttribute("utensils", userService.allUtensils());
        model.addAttribute("myAllergenIds", userService.allergenIds(user));
        model.addAttribute("myUtensilIds", userService.utensilIds(user));
        return "account";
    }

    // No box ticked: the browser sends no parameter at all, hence required = false
    @PostMapping("/account/allergies")
    public String updateAllergies(@RequestParam(name = "allergenIds", required = false) Set<Long> allergenIds,
                                  HttpSession session, RedirectAttributes redirect) {
        userService.updateAllergies(currentUser(session), allergenIds);
        redirect.addFlashAttribute("success", "Vos allergies sont enregistrées.");
        return "redirect:/account";
    }

    @PostMapping("/account/utensils")
    public String updateUtensils(@RequestParam(name = "utensilIds", required = false) Set<Long> utensilIds,
                                 HttpSession session, RedirectAttributes redirect) {
        userService.updateUtensils(currentUser(session), utensilIds);
        redirect.addFlashAttribute("success", "Vos équipements sont enregistrés.");
        return "redirect:/account";
    }

    @GetMapping("/account/recipes")
    public String myRecipes(Model model, HttpSession session) {
        User user = currentUser(session);
        addProfile(model, user, "recipes");
        model.addAttribute("myRecipes", userService.myRecipes(user));
        model.addAttribute("favorites", userService.favoriteRecipes(user));
        return "account";
    }

    // Heart button of a recipe page: adds or removes the favorite, then goes back to the recipe
    @PostMapping("/recipes/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        boolean added = userService.toggleFavorite(currentUser(session), id);
        redirect.addFlashAttribute("success", added ? "Recette ajoutée à vos favoris." : "Recette retirée de vos favoris.");
        return "redirect:/recipes/" + id;
    }

    @PostMapping("/account/favorites/{id}/remove")
    public String removeFavorite(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        User user = currentUser(session);
        if (userService.isFavorite(user, id)) {
            userService.toggleFavorite(user, id);
        }
        redirect.addFlashAttribute("success", "Recette retirée de vos favoris.");
        return "redirect:/account/recipes";
    }

    @GetMapping("/offers")
    public String offers(Model model, HttpSession session) {
        addProfile(model, currentUser(session), "offers");
        return "account";
    }

    @PostMapping("/offers/premium")
    public String upgradeToPremium(HttpSession session, RedirectAttributes redirect) {
        User user = currentUser(session);
        try {
            subscriptionService.upgradeToPremium(user, PREMIUM_MONTHS);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers";
        }
        // The session copy must follow, otherwise the header keeps showing a free account
        session.setAttribute(PublicUser.SESSION_KEY, userService.toPublicUser(user));
        redirect.addFlashAttribute("success", "Bienvenue dans CookieApp Premium !");
        return "redirect:/offers";
    }

    private User currentUser(HttpSession session) {
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        return userService.findById(me.getId());
    }

    private void addProfile(Model model, User user, String tab) {
        Subscription subscription = subscriptionService.findFor(user).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("premium", subscriptionService.isPremium(user));
        model.addAttribute("premiumUntil", subscription == null ? null : subscription.getEnd_date());
        model.addAttribute("tab", tab);
    }
}
