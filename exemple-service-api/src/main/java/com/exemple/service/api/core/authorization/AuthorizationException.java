package com.exemple.service.api.core.authorization;

import javax.ws.rs.core.Response;

import lombok.Getter;

@Getter
public class AuthorizationException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Response.Status status;

    public AuthorizationException(Response.Status status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public AuthorizationException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

}
