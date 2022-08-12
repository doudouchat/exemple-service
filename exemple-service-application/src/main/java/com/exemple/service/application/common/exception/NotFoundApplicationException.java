package com.exemple.service.application.common.exception;

import lombok.Getter;

@Getter
public class NotFoundApplicationException extends RuntimeException {

    private final String application;

    public NotFoundApplicationException(String application) {
        this.application = application;
    }
}
