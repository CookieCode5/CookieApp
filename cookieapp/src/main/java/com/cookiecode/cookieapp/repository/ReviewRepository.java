package com.cookiecode.cookieapp.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cookiecode.cookieapp.model.Review;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.Recipe;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long>{

    Optional<Review> findByAuthorAndRecipe(User u, Recipe r);
    Double averageForRecipe(Long recipeId);
    List<Recipe> topSince(LocalDateTime since);

}
