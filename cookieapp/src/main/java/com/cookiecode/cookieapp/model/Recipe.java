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
    private String image_url;

    private int base_servings = 4;
    private int preparation_time;
    private int cooking_time;

    @Enumerated(EnumType.STRING)
    private Difficulte difficulty;

    @Enumerated(EnumType.STRING)
    private Categorie category;

    private boolean vegetarian;
    private boolean vegan;
    private boolean premium;

    private LocalDateTime created_at = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Utilisateur author_id;

    public int totalTime() {
        return preparation_time + cooking_time;
    }
}
