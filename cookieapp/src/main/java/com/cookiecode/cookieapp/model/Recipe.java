package com.cookiecode.cookieapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;



@Entity
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    //URL path
    // camelCase : Spring génère quand même les colonnes image_url, base_servings, etc.
    private String imageUrl;

    private int baseServings = 4;
    private int preparationTime;
    private int cookingTime;

    @Enumerated(EnumType.STRING)
    private Difficulte difficulty;

    private boolean vegetarian;
    private boolean vegan;
    private boolean premium;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    public int totalTime() {
        return preparationTime + cookingTime;
    }
}
