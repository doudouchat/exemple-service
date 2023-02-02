package com.exemple.service.customer.login;

import java.util.Optional;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public interface LoginService {

    Optional<UUID> get(@NotBlank String username);

}
