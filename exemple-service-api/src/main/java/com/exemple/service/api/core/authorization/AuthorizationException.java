package com.exemple.service.api.core.authorization;

public class AuthorizationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

}
