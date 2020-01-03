package com.exemple.service.application.detail;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.application.common.model.ApplicationDetail;

public interface ApplicationDetailService {

    void put(@NotBlank String application, @Valid @NotNull ApplicationDetail detail);

    ApplicationDetail get(String application);

}
