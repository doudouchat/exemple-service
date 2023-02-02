package com.exemple.service.resource.account.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UsernameNotUniqueException extends RuntimeException {

    private final String username;

}
