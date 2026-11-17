package com.cookiecode.cookieapp.model;
import com.cookiecode.cookieapp.model.enums.Role;

import java.time.LocalDate;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter 
@ToString 
@Entity 
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (unique=true)
    private String username;

    @Column  (unique=true)
    private String email;

    @Column
    private String password_hash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private LocalDate registration_date;
    
}
