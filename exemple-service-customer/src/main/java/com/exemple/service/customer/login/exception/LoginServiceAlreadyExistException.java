package com.exemple.service.customer.login.exception;

public class LoginServiceAlreadyExistException extends LoginServiceException {

    private static final long serialVersionUID = 1L;

    private final String username;

    public LoginServiceAlreadyExistException(String username, Throwable cause) {
        super(cause);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
