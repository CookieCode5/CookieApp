package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.ReviewForm;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/recipes/{id}/reviews")
    public String rate(@PathVariable Long id, @Valid @ModelAttribute ReviewForm form, BindingResult result,
                       HttpSession session, RedirectAttributes redirect) {
        PublicUser me = (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", "La note doit être comprise entre 1 et 5.");
        } else {
            try {
                reviewService.rate(me.getId(), id, form.getRating(), form.getComment());
                redirect.addFlashAttribute("success", "Merci pour votre avis !");
            } catch (BusinessException e) {
                redirect.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/recipes/" + id + "#reviews";
    }

    @PostMapping("/recipes/{id}/reviews/{reviewId}/delete")
    public String delete(@PathVariable Long id, @PathVariable Long reviewId,
                         HttpSession session, RedirectAttributes redirect) {
        try {
            reviewService.delete(reviewId, (PublicUser) session.getAttribute(PublicUser.SESSION_KEY));
            redirect.addFlashAttribute("success", "Avis supprimé.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recipes/" + id + "#reviews";
    }
}