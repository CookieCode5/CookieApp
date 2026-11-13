package model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.ToString;
import jakarta.persistence.GenerationType;

public class Review {

    // Clé primaire
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column 
    private int rating;

    @Column 
    private String comment;

    @Column 
    private LocalDate created_at;

    // Clé secondaire
    @ToString.Exclude
    @ManyToOne 
    @JoinColumn(name = "auteur_id")
    private User auteur;

    


}
