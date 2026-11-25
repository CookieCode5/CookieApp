package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.Role;
import com.cookiecode.cookieapp.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

// Real service and real H2 database; @Transactional rolls every test back, so nothing stays in the database
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private static RegisterDTO form(String username, String email, String password, String confirm) {
        RegisterDTO form = new RegisterDTO();
        form.setUsername(username);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(confirm);
        return form;
    }

    private User registerChef() {
        return userService.register(form("chef", "chef@example.com", "password123", "password123"));
    }

    @Test
    void register_createsAUserWithAHashedPasswordAndAFreeSubscription() {
        User user = userService.register(form(" chef ", " Chef@Example.com ", "password123", "password123"));

        assertEquals("chef", user.getUsername());
        assertEquals("chef@example.com", user.getEmail());
        assertEquals(Role.USER, user.getRole());
        assertNotEquals("password123", user.getPassword_hash());
        assertTrue(subscriptionRepository.findByUser(user).isPresent());
        assertFalse(subscriptionService.isPremium(user));
    }

    @Test
    void register_refusesAnEmailAlreadyUsed() {
        registerChef();

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("otherChef", "chef@example.com", "password123", "password123")));

        assertEquals("email", e.getField());
    }

    @Test
    void register_refusesAUsernameAlreadyUsed() {
        registerChef();

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("chef", "other@example.com", "password123", "password123")));

        assertEquals("username", e.getField());
    }

    @Test
    void register_refusesDifferentPasswords() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.register(form("chef", "chef@example.com", "password123", "differentPassword")));

        assertEquals("confirmPassword", e.getField());
    }

    @Test
    void authenticate_acceptsTheRightPasswordWhateverTheEmailCase() {
        registerChef();

        User user = userService.authenticate(" Chef@Example.com", "password123");

        assertEquals("chef", user.getUsername());
    }

    @Test
    void authenticate_refusesAWrongPassword() {
        registerChef();

        assertThrows(BusinessException.class, () -> userService.authenticate("chef@example.com", "wrongPassword"));
    }

    @Test
    void authenticate_refusesAnUnknownEmail() {
        assertThrows(BusinessException.class, () -> userService.authenticate("unknown@example.com", "password123"));
    }

    @Test
    void toPublicUser_keepsNoPassword() {
        User user = registerChef();

        PublicUser publicUser = userService.toPublicUser(user);

        assertEquals(user.getId(), publicUser.getId());
        assertEquals("chef", publicUser.getUsername());
        assertFalse(publicUser.isPremium());
        assertFalse(publicUser.isAdmin());
    }
}
