package com.exemple.service.api.common.model;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.HeaderParam;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
