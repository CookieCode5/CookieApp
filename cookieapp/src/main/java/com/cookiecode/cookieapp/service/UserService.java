package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.Role;
import com.cookiecode.cookieapp.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UtilisateurRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;

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

    static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
