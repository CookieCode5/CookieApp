package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterDTO form,
                           BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form);
        } catch (BusinessException e) {
            reject(result, e);
            return "auth/register";
        }
        redirect.addFlashAttribute("success", "Votre compte est créé ! Vous pouvez vous connecter.");
        return "redirect:/login";
    }

    private static void reject(BindingResult result, BusinessException e) {
        if (e.getField() != null) {
            result.rejectValue(e.getField(), "business", e.getMessage());
        } else {
            result.reject("business", e.getMessage());
        }
    }
}
