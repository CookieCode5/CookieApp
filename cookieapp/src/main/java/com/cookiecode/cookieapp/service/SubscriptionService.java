package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.model.Subscription;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.SubscriptionType;
import com.cookiecode.cookieapp.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    // RG-U1: every new account starts with a FREE subscription, with no end date
    @Transactional
    public Subscription createFree(User user) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setSubType(SubscriptionType.FREE);
        subscription.setStart_date(LocalDate.now());
        return subscriptionRepository.save(subscription);
    }

    // A PREMIUM subscription counts until its end date; no end date means it never expires
    @Transactional(readOnly = true)
    public boolean isPremium(User user) {
        return subscriptionRepository.findByUser(user)
                .filter(s -> s.getSubType() == SubscriptionType.PREMIUM)
                .filter(s -> s.getEnd_date() == null || !s.getEnd_date().isBefore(LocalDate.now()))
                .isPresent();
    }
}
