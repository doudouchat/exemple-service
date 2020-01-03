package com.exemple.service.api.common.model;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.HeaderParam;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

public class SchemaBeanParam extends ApplicationBeanParam {

    public static final String VERSION_HEADER = "version";

    @NotBlank
    @Parameter(name = VERSION_HEADER, in = ParameterIn.HEADER)
    private final String version;

    public SchemaBeanParam(@HeaderParam(APP_HEADER) String app, @HeaderParam(VERSION_HEADER) String version) {

        super(app);

        this.version = version;

    }

    public String getVersion() {
        return version;
    }

}
