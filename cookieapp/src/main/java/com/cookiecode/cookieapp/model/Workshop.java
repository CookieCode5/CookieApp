package com.cookiecode.cookieapp.model;

import java.time.LocalDateTime;

import com.cookiecode.cookieapp.model.enums.WorkshopType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter 
@Entity 
@ToString 
public class Workshop {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String title;

    @Column (name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column
    private String location;

    @Enumerated(EnumType.STRING)
    @Column
    private WorkshopType type;

    @Column
    private int max_seats;

    @ManyToOne 
    @JoinColumn(name = "recipe_id")
    @ToString.Exclude
    private Recipe recipe;

    @ManyToOne 
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    public int remainingSeats() {
        int bookedSeats = 0;
        return Math.max(0, max_seats-bookedSeats);
    }

    public boolean isFull(){
        return remainingSeats() <= 0;
    }

}
