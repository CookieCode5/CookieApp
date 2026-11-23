package com.cookiecode.cookieapp.controller;

import com.cookiecode.cookieapp.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;

    @GetMapping("/users/{id}")
    public String profile(@PathVariable Long id, Model model) {
        model.addAttribute("profile", profileService.publicProfile(id));
        return "users/profile";
    }
}
