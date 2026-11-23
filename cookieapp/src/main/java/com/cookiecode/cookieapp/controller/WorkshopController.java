package com.cookiecode.cookieapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    // Every /workshops page needs a logged-in user (LoginInterceptor), so currentUser is never null here
    @GetMapping("/workshops")
    public String list(Model model, HttpSession session) {
        model.addAttribute("workshops", workshopService.mine(currentUser(session).getId()));
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
        Workshop w = workshopService.getForMember(id, currentUser(session).getId());
        fillDetail(w, model, session);
        if (w.getOrganizer().getId() == currentUser(session).getId()) {
            model.addAttribute("inviteLink", ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/workshops/join/{code}").buildAndExpand(w.getInviteCode()).toUriString());
        }
        return "workshops/detail";
    }

    // Invitation link: a member goes straight to the group, anyone else sees it with a "join" button.
    // A GET never joins, so a link preview in a chat app cannot add someone by accident.
    @GetMapping("/workshops/join/{code}")
    public String invitation(@PathVariable String code, Model model, HttpSession session) {
        Workshop w = workshopService.getByInvite(code);
        if (workshopService.isMember(w, currentUser(session).getId())) {
            return "redirect:/workshops/" + w.getId();
        }
        fillDetail(w, model, session);
        model.addAttribute("invited", true);
        return "workshops/detail";
    }

    @PostMapping("/workshops/join/{code}")
    public String join(@PathVariable String code, HttpSession session, RedirectAttributes redirect) {
        try {
            Workshop w = workshopService.join(code, currentUser(session).getId());
            redirect.addFlashAttribute("success", "Vous avez rejoint l'équipe : les étapes ont été réparties à nouveau.");
            return "redirect:/workshops/" + w.getId();
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/workshops/join/" + code;
        }
    }

    @PostMapping("/workshops/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            workshopService.delete(id, currentUser(session).getId());
            redirect.addFlashAttribute("success", "L'atelier a été supprimé.");
            return "redirect:/workshops";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/workshops/" + id;
        }
    }

    @PostMapping("/workshops/{id}/leave")
    public String leave(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            workshopService.leave(id, currentUser(session).getId());
            redirect.addFlashAttribute("success", "Vous avez quitté l'équipe.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // Not a member any more: the group page would now answer 404
        return "redirect:/workshops";
    }

    private void fillDetail(Workshop w, Model model, HttpSession session) {
        Long me = currentUser(session).getId();
        model.addAttribute("workshop", w);
        model.addAttribute("plan", workshopService.plan(w));
        model.addAttribute("isOrganizer", w.getOrganizer().getId() == me);
        model.addAttribute("isParticipant", workshopService.isParticipant(w, me));
    }

    private static PublicUser currentUser(HttpSession session) {
        return (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
    }
}