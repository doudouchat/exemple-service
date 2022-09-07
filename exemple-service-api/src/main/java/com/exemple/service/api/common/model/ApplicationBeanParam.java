package com.exemple.service.api.common.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.HeaderParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ApplicationBeanParam {

    public static final String APP_HEADER = "app";

    @NotBlank
    @HeaderParam(APP_HEADER)
    @Parameter(name = APP_HEADER, in = ParameterIn.HEADER)
    private final String app;

}
