package com.cookiecode.cookieapp.dto;

import com.cookiecode.cookieapp.model.enums.Category;

public record RecipeSearchCriteria(
        String q,
        Category category,
        Boolean vegetarian,
        Integer maxTime,
        String sort
) {}