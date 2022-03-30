package com.exemple.service.customer.login;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface LoginResource {

    void save(@NotNull UUID id, @NotBlank String username);

    void delete(@NotBlank String username);

    Optional<UUID> get(@NotBlank String username);

}
