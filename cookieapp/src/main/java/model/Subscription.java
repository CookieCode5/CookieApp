package model;
import model.enums.SubscriptionType;
import java.time.LocalDate;

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

    // Clé secondaire
    @ToString.Exclude
    @OneToOne 
    @JoinColumn(name="user_id", unique=true)
    private User user;

}
