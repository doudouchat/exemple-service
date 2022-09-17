package com.exemple.service.api.core.authorization;

import javax.ws.rs.core.Response;

import lombok.Getter;

@Getter
public class AuthorizationException extends Exception {

    private final Response.Status status;

    public AuthorizationException(Response.Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public AuthorizationException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

}
