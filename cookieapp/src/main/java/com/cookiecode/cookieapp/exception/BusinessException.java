package com.cookiecode.cookieapp.exception;

/**
 * A business rule was broken. The message is shown to the user as is, so it is written in French.
 * field is the form field the error belongs to, or null for a global error.
 */
public class BusinessException extends RuntimeException {

    private final String field;

    public BusinessException(String message) {
        this(null, message);
    }

    public BusinessException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
