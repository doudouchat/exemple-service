package com.exemple.service.resource.login.exception;

import java.text.MessageFormat;

public class LoginResourceExistException extends LoginResourceException {

    protected static final String EXCEPTION_MESSAGE = "Login {0} already exists";

    private static final long serialVersionUID = 1L;

    private final String login;

    public LoginResourceExistException(String login) {
        super(MessageFormat.format(EXCEPTION_MESSAGE, login));
        this.login = login;
    }

    public String getLogin() {
        return this.login;
    }

}
