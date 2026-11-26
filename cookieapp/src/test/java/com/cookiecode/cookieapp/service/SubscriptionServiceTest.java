package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.RegisterDTO;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Subscription;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.Role;
import com.cookiecode.cookieapp.model.enums.SubscriptionType;
import com.cookiecode.cookieapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Real services and real H2 database; @Transactional rolls every test back
@SpringBootTest
@Transactional
class SubscriptionServiceTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User registerChef() {
        RegisterDTO form = new RegisterDTO();
        form.setUsername("chef");
        form.setEmail("chef@example.com");
        form.setPassword("password123");
        form.setConfirmPassword("password123");
        return userService.register(form);
    }

    @Test
    void aNewAccountIsFree() {
        User user = registerChef();

        assertFalse(subscriptionService.isPremium(user));
    }

    @Test
    void upgradeToPremium_givesOneMonthOfPremium() {
        User user = registerChef();

        Subscription subscription = subscriptionService.upgradeToPremium(user, 1);

        assertEquals(SubscriptionType.PREMIUM, subscription.getSubType());
        assertEquals(LocalDate.now().plusMonths(1), subscription.getEnd_date());
        assertTrue(subscriptionService.isPremium(user));
    }

    @Test
    void upgradeToPremium_refusesAnAccountThatIsAlreadyPremium() {
        User user = registerChef();
        subscriptionService.upgradeToPremium(user, 1);

        assertThrows(BusinessException.class, () -> subscriptionService.upgradeToPremium(user, 1));
    }

    @Test
    void upgradeToPremium_createsTheSubscriptionWhenTheAccountHasNone() {
        User user = new User();
        user.setUsername("noSubscription");
        user.setEmail("no-subscription@example.com");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        subscriptionService.upgradeToPremium(user, 1);

        assertTrue(subscriptionService.isPremium(user));
    }
}
