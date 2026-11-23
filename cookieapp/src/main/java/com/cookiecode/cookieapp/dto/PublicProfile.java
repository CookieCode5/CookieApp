package com.cookiecode.cookieapp.dto;

import com.cookiecode.cookieapp.model.Recipe;

import java.time.LocalDate;
import java.util.List;

public record PublicProfile(Long id, String username, LocalDate registrationDate, boolean premium,
                            List<Recipe> recipes, Double averageReceived) {}