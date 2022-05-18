package com.exemple.service.application.detail;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.fasterxml.jackson.databind.JsonNode;

public interface ApplicationDetailService {

    void put(@NotBlank String application, @NotNull JsonNode detail);

    Optional<ApplicationDetail> get(String application);

}
