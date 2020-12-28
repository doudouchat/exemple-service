package com.exemple.service.api.core.authorization;

import javax.ws.rs.core.Response;

public class AuthorizationException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Response.Status status;

    public AuthorizationException(Throwable cause) {
        super(cause);
        this.status = Response.Status.FORBIDDEN;
    }

    public AuthorizationException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
        this.status = Response.Status.FORBIDDEN;
    }

    public Response.Status getStatus() {
        return status;
    }

}
