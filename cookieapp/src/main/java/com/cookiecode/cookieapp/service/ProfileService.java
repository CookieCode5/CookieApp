package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.PublicProfile;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.ReviewRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final ReviewRepository reviewRepository;
    private final SubscriptionService subscriptionService;

    // Never the email nor the password
    @Transactional(readOnly = true)
    public PublicProfile publicProfile(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new PublicProfile(u.getId(), u.getUsername(), u.getRegistration_date(),
                subscriptionService.isPremium(u),
                recipeRepository.findByAuthorIdOrderByCreatedAtDesc(u.getId()),
                reviewRepository.averageReceivedBy(u.getId()));
    }
}