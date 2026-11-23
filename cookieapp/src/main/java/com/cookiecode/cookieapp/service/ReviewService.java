package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.Review;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.ReviewRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    // RG-A1: one review per (user, recipe), the second one replaces the first; no review of your own recipe
    @Transactional
    public Review rate(Long userId, Long recipeId, int rating, String comment) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (recipe.getAuthor() != null && recipe.getAuthor().getId() == userId) {
            throw new BusinessException("Vous ne pouvez pas noter votre propre recette.");
        }
        if (rating < 1 || rating > 5) {
            throw new BusinessException("rating", "La note doit être comprise entre 1 et 5.");
        }
        Review review = reviewRepository.findByUserIdAndRecipeId(userId, recipeId).orElseGet(() -> {
            Review r = new Review();
            r.setRecipe(recipe);
            r.setUser(userRepository.getReferenceById(userId));
            return r;
        });
        review.setRating(rating);
        review.setComment(comment == null || comment.isBlank() ? null : comment.trim());
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);
        refreshAverage(recipe);
        return review;
    }

    // Only the author of the review or an ADMIN can delete it
    @Transactional
    public void delete(Long reviewId, PublicUser me) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (review.getUser().getId() != me.getId() && !me.isAdmin()) {
            throw new BusinessException("Vous ne pouvez supprimer que vos propres avis.");
        }
        Recipe recipe = review.getRecipe();
        recipe.getReviews().remove(review);
        reviewRepository.delete(review);
        refreshAverage(recipe);
    }

    // RG-A2: called after every add / update / delete
    @Transactional
    public void refreshAverage(Recipe recipe) {
        Double avg = reviewRepository.averageForRecipe(recipe.getId());
        recipe.setAverageRating(avg == null ? 0 : Math.round(avg * 10) / 10.0);
        recipe.setReviewCount((int) reviewRepository.countByRecipeId(recipe.getId()));
    }

    @Transactional(readOnly = true)
    public List<Review> forRecipe(Long recipeId) {
        return reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findMine(Long userId, Long recipeId) {
        return reviewRepository.findByUserIdAndRecipeId(userId, recipeId);
    }

    // 5 → 1 stars, with 0 for the missing ones (step 4)
    @Transactional(readOnly = true)
    public Map<Integer, Long> starDistribution(Long recipeId) {
        Map<Integer, Long> stars = new LinkedHashMap<>();
        for (int s = 5; s >= 1; s--) stars.put(s, 0L);
        for (Object[] row : reviewRepository.countByRating(recipeId)) {
            stars.put((Integer) row[0], (Long) row[1]);
        }
        return stars;
    }
}