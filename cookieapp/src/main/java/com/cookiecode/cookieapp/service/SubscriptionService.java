package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Subscription;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.enums.SubscriptionType;
import com.cookiecode.cookieapp.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final DateTimeFormatter FRENCH_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    @Transactional(readOnly = true)
    public Optional<Subscription> findFor(User user) {
        return subscriptionRepository.findByUser(user);
    }

    // A PREMIUM subscription counts until its end date; no end date means it never expires
    @Transactional(readOnly = true)
    public boolean isPremium(User user) {
        return subscriptionRepository.findByUser(user)
                .filter(s -> s.getSubType() == SubscriptionType.PREMIUM)
                .filter(s -> s.getEnd_date() == null || !s.getEnd_date().isBefore(LocalDate.now()))
                .isPresent();
    }

    // Simulated payment (RG-S1): the 1-1 subscription switches to PREMIUM for the given number of months
    @Transactional
    public Subscription upgradeToPremium(User user, int months) {
        if (months < 1) {
            throw new BusinessException("Choisissez une durée d'au moins un mois.");
        }
        Subscription subscription = subscriptionRepository.findByUser(user).orElseGet(() -> {
            Subscription created = new Subscription();
            created.setUser(user);
            return created;
        });
        if (isPremium(user)) {
            String until = subscription.getEnd_date() == null
                    ? "" : " jusqu'au " + subscription.getEnd_date().format(FRENCH_DATE);
            throw new BusinessException("Votre compte est déjà Premium" + until + ".");
        }

        LocalDate today = LocalDate.now();
        subscription.setSubType(SubscriptionType.PREMIUM);
        subscription.setStart_date(today);
        subscription.setEnd_date(today.plusMonths(months));
        return subscriptionRepository.save(subscription);
    }
}
