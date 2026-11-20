package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.config.LoginInterceptor;
import com.cookiecode.cookieapp.dto.LoginDTO;
import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public String registerForm(Model model, HttpSession session) {
        if (isLoggedIn(session)) {
            return "redirect:/";
        }
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

    @GetMapping("/login")
    public String loginForm(Model model, HttpSession session) {
        if (isLoggedIn(session)) {
            return "redirect:/";
        }
        model.addAttribute("loginForm", new LoginDTO());
        model.addAttribute("loginRequired", session.getAttribute(LoginInterceptor.REDIRECT_KEY) != null);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginDTO form,
                        BindingResult result,
                        HttpServletRequest request,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/login";
        }
        PublicUser user;
        try {
            user = userService.toPublicUser(userService.authenticate(form.getEmail(), form.getPassword()));
        } catch (BusinessException e) {
            reject(result, e);
            return "auth/login";
        }

        HttpSession session = request.getSession();
        // New session id after login, against session fixation
        request.changeSessionId();
        session.setAttribute(PublicUser.SESSION_KEY, user);

        // Send the user back to the page LoginInterceptor stopped, if any
        String target = (String) session.getAttribute(LoginInterceptor.REDIRECT_KEY);
        session.removeAttribute(LoginInterceptor.REDIRECT_KEY);
        if (target == null || !target.startsWith("/") || target.startsWith("//")) {
            target = "/";
        }
        redirect.addFlashAttribute("success", "Bonjour " + user.getUsername() + " !");
        return "redirect:" + target;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("success", "Vous êtes déconnecté(e). À bientôt !");
        return "redirect:/";
    }

    private static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(PublicUser.SESSION_KEY) != null;
    }

    private static void reject(BindingResult result, BusinessException e) {
        if (e.getField() != null) {
            result.rejectValue(e.getField(), "business", e.getMessage());
        } else {
            result.reject("business", e.getMessage());
        }
    }
}
