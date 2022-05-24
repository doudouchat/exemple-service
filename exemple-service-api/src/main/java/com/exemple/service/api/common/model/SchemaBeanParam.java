package com.exemple.service.api.common.model;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.HeaderParam;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SchemaBeanParam {

    public static final String APP_HEADER = "app";

    public static final String VERSION_HEADER = "version";

    @NotBlank
    @HeaderParam(APP_HEADER)
    @Parameter(name = APP_HEADER, in = ParameterIn.HEADER)
    private final String app;

    @NotBlank
    @HeaderParam(VERSION_HEADER)
    @Parameter(name = VERSION_HEADER, in = ParameterIn.HEADER)
    private final String version;

}
