package com.cookiecode.cookieapp.repository;

import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByOrderByCreatedAtDesc();
    Page<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Recipe> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Query("""
        select r from Recipe r
        where (:q is null or lower(r.title) like lower(concat('%', :q, '%')))
          and (:category is null or r.category = :category)
          and (:vegetarian is null or (:vegetarian = true and r.vegetarian = true))
          and (:maxTime is null or (r.preparationTime + r.cookingTime) <= :maxTime)
        order by
          case when :sort = 'rating' then r.averageRating end desc,
          case when :sort = 'time'   then (r.preparationTime + r.cookingTime) end asc,
          case when :sort = 'recent' then r.createdAt end desc
        """)
    List<Recipe> search(@Param("q") String q,
                        @Param("category") Category category,
                        @Param("vegetarian") Boolean vegetarian,
                        @Param("maxTime") Integer maxTime,
                        @Param("sort") String sort);
}