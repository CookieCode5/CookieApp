package com.cookiecode.cookieapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * What we keep in the HTTP session after login: never the User entity, never the password.
 */
@Getter
@AllArgsConstructor
public class PublicUser implements Serializable {

    public static final String SESSION_KEY = "user";

    private final Long id;
    private final String username;
    private final String email;
    private final boolean premium;
    private final boolean admin;
}
