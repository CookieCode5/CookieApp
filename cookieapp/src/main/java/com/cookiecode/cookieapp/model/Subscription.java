package com.cookiecode.cookieapp.model;
import com.cookiecode.cookieapp.model.enums.SubscriptionType;
import java.time.LocalDate;

import org.springframework.cglib.core.Local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter 
@ToString 
@Entity
public class Subscription {

    // CLé primaire
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private SubscriptionType subType;
    
    @Column
    private LocalDate start_date;

    @Column
    private LocalDate end_date;

    @Column
    private int daily_searches;

    @Column
    private LocalDate last_search_date;

    private static final int daily_free_search_limit = 5;

    // Clé secondaire
    @ToString.Exclude
    @OneToOne 
    @JoinColumn(name="user_id", unique=true)
    private User user;

    public boolean isActive(){
        LocalDate today = LocalDate.now();

        if (start_date == null || today.isBefore(start_date)){
            return false;
        }

        return end_date == null || !today.isAfter(end_date);

    }

    public boolean canStartSearch() {

        if (!isActive()){
            return false;
        }

        if (subType == SubscriptionType.PREMIUM) {
            return true;
        }

        LocalDate today = LocalDate.now();
        if (last_search_date == null || !today.equals(last_search_date)){
            return true;
        }

        return daily_searches < daily_free_search_limit;

    }

}
