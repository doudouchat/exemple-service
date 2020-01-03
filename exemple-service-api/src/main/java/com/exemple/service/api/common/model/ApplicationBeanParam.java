package com.exemple.service.api.common.model;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.HeaderParam;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

public class ApplicationBeanParam {

    public static final String APP_HEADER = "app";

    @NotBlank
    @Parameter(name = APP_HEADER, in = ParameterIn.HEADER)
    private final String app;

    public ApplicationBeanParam(@HeaderParam(APP_HEADER) String app) {

        this.app = app;

    }

    public String getApp() {
        return app;
    }

}
