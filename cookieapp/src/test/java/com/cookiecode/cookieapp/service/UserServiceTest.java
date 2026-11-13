package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.Role;
import com.cookiecode.cookieapp.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UtilisateurRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, subscriptionService, passwordEncoder);
    }

    private static RegisterDTO form(String username, String email, String password, String confirm) {
        RegisterDTO form = new RegisterDTO();
        form.setUsername(username);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(confirm);
        return form;
    }

    @Test
    void register_hashesPasswordNormalizesEmailAndCreatesFreeSubscription() {
        when(userRepository.existsByEmail("amina@test.fr")).thenReturn(false);
        when(userRepository.existsByUsername("amina")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(form(" amina ", " Amina@Test.fr ", "motdepasse", "motdepasse"));

        assertEquals("amina", saved.getUsername());
        assertEquals("amina@test.fr", saved.getEmail());
        assertNotEquals("motdepasse", saved.getPassword_hash());
        assertTrue(passwordEncoder.matches("motdepasse", saved.getPassword_hash()));
        assertEquals(Role.USER, saved.getRole());
        verify(subscriptionService).createFree(saved);
    }

    @Test
    void register_refusesAnEmailAlreadyUsed() {
        when(userRepository.existsByEmail("amina@test.fr")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("amina", "amina@test.fr", "motdepasse", "motdepasse")));

        assertEquals("email", e.getField());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void register_refusesAUsernameAlreadyUsed() {
        when(userRepository.existsByEmail("amina@test.fr")).thenReturn(false);
        when(userRepository.existsByUsername("amina")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("amina", "amina@test.fr", "motdepasse", "motdepasse")));

        assertEquals("username", e.getField());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_refusesDifferentPasswords() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("amina", "amina@test.fr", "motdepasse", "autrechose")));

        assertEquals("confirmPassword", e.getField());
        verifyNoInteractions(userRepository, subscriptionService);
    }

    private User existingUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("amina");
        user.setEmail("amina@test.fr");
        user.setPassword_hash(passwordEncoder.encode("motdepasse"));
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void authenticate_acceptsTheRightPasswordWhateverTheEmailCase() {
        when(userRepository.findByEmail("amina@test.fr")).thenReturn(Optional.of(existingUser()));

        User user = userService.authenticate(" Amina@test.fr", "motdepasse");

        assertEquals("amina", user.getUsername());
    }

    @Test
    void authenticate_refusesAWrongPassword() {
        when(userRepository.findByEmail("amina@test.fr")).thenReturn(Optional.of(existingUser()));

        assertThrows(BusinessException.class, () -> userService.authenticate("amina@test.fr", "mauvais"));
    }

    @Test
    void authenticate_refusesAnUnknownEmail() {
        when(userRepository.findByEmail("inconnu@test.fr")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userService.authenticate("inconnu@test.fr", "motdepasse"));
    }

    @Test
    void toPublicUser_keepsNoPasswordAndReadsPremiumFromTheSubscription() {
        User user = existingUser();
        when(subscriptionService.isPremium(user)).thenReturn(true);

        PublicUser publicUser = userService.toPublicUser(user);

        assertEquals(1L, publicUser.getId());
        assertEquals("amina", publicUser.getUsername());
        assertTrue(publicUser.isPremium());
        assertFalse(publicUser.isAdmin());
    }
}
