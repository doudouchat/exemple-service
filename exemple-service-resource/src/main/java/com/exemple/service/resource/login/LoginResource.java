package com.exemple.service.resource.login;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.resource.login.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.login.model.LoginEntity;

public interface LoginResource {

    Optional<LoginEntity> get(@NotBlank String username);

    void save(@NotNull LoginEntity login) throws UsernameAlreadyExistsException;

    void delete(@NotBlank String username);
}
