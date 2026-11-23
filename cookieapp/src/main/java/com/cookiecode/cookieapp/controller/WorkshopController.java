package com.cookiecode.cookieapp.controller;

import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.WorkshopForm;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Workshop;
import com.cookiecode.cookieapp.service.RecipeService;
import com.cookiecode.cookieapp.service.WorkshopService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;
    private final RecipeService recipeService;

    @GetMapping("/workshops")
    public String list(Model model, HttpSession session) {
        PublicUser me = currentUser(session);
        model.addAttribute("workshops", workshopService.all());
        model.addAttribute("myIds", me == null ? Set.of() : workshopService.myWorkshopIds(me.getId()));
        return "workshops/list";
    }

    // ?recipeId= pre-selects the recipe when coming from the "Cuisiner à plusieurs" button of a recipe page
    @GetMapping("/workshops/new")
    public String newForm(@RequestParam(required = false) Long recipeId, Model model) {
        WorkshopForm form = new WorkshopForm();
        form.setRecipeId(recipeId);
        model.addAttribute("workshopForm", form);
        model.addAttribute("recipes", recipeService.lister());
        return "workshops/form";
    }

    @PostMapping("/workshops/new")
    public String create(@Valid @ModelAttribute("workshopForm") WorkshopForm form, BindingResult result,
                         Model model, HttpSession session, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Workshop w = workshopService.create(form, currentUser(session).getId());
                redirect.addFlashAttribute("success", "Votre atelier est créé : invitez d'autres cuisiniers !");
                return "redirect:/workshops/" + w.getId();
            } catch (BusinessException e) {
                result.rejectValue("recipeId", "business", e.getMessage());
            }
        }
        model.addAttribute("recipes", recipeService.lister());
        return "workshops/form";
    }

    @GetMapping("/workshops/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Workshop w = workshopService.get(id);
        PublicUser me = currentUser(session);
        model.addAttribute("workshop", w);
        model.addAttribute("plan", workshopService.plan(w));
        model.addAttribute("isOrganizer", me != null && w.getOrganizer().getId() == me.getId());
        model.addAttribute("isParticipant", me != null && workshopService.isParticipant(w, me.getId()));
        return "workshops/detail";
    }

    @PostMapping("/workshops/{id}/join")
    public String join(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            workshopService.join(id, currentUser(session).getId());
            redirect.addFlashAttribute("success", "Vous avez rejoint l'équipe : les étapes ont été réparties à nouveau.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/workshops/" + id;
    }

    @PostMapping("/workshops/{id}/leave")
    public String leave(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            workshopService.leave(id, currentUser(session).getId());
            redirect.addFlashAttribute("success", "Vous avez quitté l'équipe.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/workshops/" + id;
    }

    private static PublicUser currentUser(HttpSession session) {
        return (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
    }
}