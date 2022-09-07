package com.exemple.service.customer.login;

import java.util.Optional;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface LoginResource {

    void save(@NotNull UUID id, @NotBlank String username);

    void delete(@NotBlank String username);

    Optional<UUID> get(@NotBlank String username);

}
