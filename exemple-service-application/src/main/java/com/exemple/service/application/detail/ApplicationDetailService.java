package com.exemple.service.application.detail;

import java.util.Optional;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface ApplicationDetailService {

    void put(@NotBlank String application, @NotNull JsonNode detail);

    Optional<ApplicationDetail> get(String application);

}
