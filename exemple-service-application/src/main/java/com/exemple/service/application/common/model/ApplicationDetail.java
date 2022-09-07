package com.exemple.service.application.common.model;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
public class ApplicationDetail {

    @NotBlank
    private final String keyspace;

    @NotBlank
    private final String company;

    @NotEmpty
    @Singular
    private final Set<String> clientIds;

}
