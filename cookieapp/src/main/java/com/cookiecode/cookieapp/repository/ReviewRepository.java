package com.cookiecode.cookieapp.repository;

import com.cookiecode.cookieapp.model.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndRecipeId(Long userId, Long recipeId);
    List<Review> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);
    long countByRecipeId(Long recipeId);

    @Query("select avg(r.rating) from Review r where r.recipe.id = :recipeId")
    Double averageForRecipe(@Param("recipeId") Long recipeId);

    @Query("select r.rating, count(r) from Review r where r.recipe.id = :recipeId group by r.rating")
    List<Object[]> countByRating(@Param("recipeId") Long recipeId);

    // Ids only: grouping by the whole entity fails in H2 because of the TEXT columns
    @Query("""
           select r.recipe.id from Review r
           where r.createdAt >= :since
           group by r.recipe.id
           order by avg(r.rating) desc, count(r) desc""")
    List<Long> topRecipeIdsSince(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("select avg(r.rating) from Review r where r.recipe.author.id = :authorId")
    Double averageReceivedBy(@Param("authorId") Long authorId);
}