package com.exemple.service.api.login;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.api.core.check.AppAndVersionCheck;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.login.LoginResource;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;

@Path("/v1/logins")
@OpenAPIDefinition(tags = @Tag(name = "login"))
@Component
@RequiredArgsConstructor
public class LoginApi {

    private static final String LOGIN_SCHEMA = "Login";

    private final LoginResource loginResource;

    @Context
    private AuthorizationCheckService authorizationCheckService;

    @HEAD
    @Path("/{username}")
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = ApplicationBeanParam.class))
    @RolesAllowed("login:head")
    @AppAndVersionCheck(optionalVersion = true)
    public Response check(@PathParam("username") String username) {

        if (loginResource.get(username).isPresent()) {

            return Response.status(Status.NO_CONTENT).build();
        }

        return Response.status(Status.NOT_FOUND).build();

    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Login Data", responseCode = "200", content = @Content(schema = @Schema(ref = LOGIN_SCHEMA))),
            @ApiResponse(description = "Login is not found", responseCode = "404"),
            @ApiResponse(description = "Login is not accessible", responseCode = "403")

    })
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = ApplicationBeanParam.class))
    @RolesAllowed("login:read")
    @AppAndVersionCheck(optionalVersion = true)
    public UUID get(@NotNull @PathParam("username") String username) {

        authorizationCheckService.verifyLogin(username);

        return loginResource.get(username).orElseThrow(NotFoundException::new);

    }

}
