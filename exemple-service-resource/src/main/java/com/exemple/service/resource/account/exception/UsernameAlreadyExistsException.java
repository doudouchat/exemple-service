package com.exemple.service.resource.account.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UsernameAlreadyExistsException extends RuntimeException {

    private final String username;

}
