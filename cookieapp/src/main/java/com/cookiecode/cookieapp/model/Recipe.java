package com.cookiecode.cookieapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cookiecode.cookieapp.model.enums.Difficulty;



@Entity
@Getter
@Setter
@NoArgsConstructor
public class Recipe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id")
    private User author;

    private String title;
    @Column(columnDefinition = "TEXT") private String description;
    private String imageUrl;
    private int baseServings;
    private int preparationTime;
    private int cookingTime;

    @Enumerated(EnumType.STRING) private Difficulty difficulty;
    private boolean vegetarian;
    private boolean vegan;
    private boolean premium;
    private double averageRating;
    private int reviewCount;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder")
    private List<Step> steps = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "recipe_utensil",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "utensil_id"))
    private Set<Utensil> utensils = new HashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public int totalTime() { return preparationTime + cookingTime; }

    public Set<Allergen> allergens() {
        return ingredients.stream()
            .flatMap(ri -> ri.getIngredient().getAllergens().stream())
            .collect(Collectors.toSet());
    }
}