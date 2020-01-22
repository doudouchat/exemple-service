package com.exemple.service.api.login;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.PatchUtils;
import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationContextService;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/v1/logins")
@OpenAPIDefinition(tags = @Tag(name = "login"))
@Component
public class LoginApi {

    private static final String LOGIN_SCHEMA = "Login";

    @Autowired
    private LoginService loginService;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private AuthorizationContextService authorizationContextService;

    @Context
    private ContainerRequestContext servletContext;

    @HEAD
    @Path("/{login}")
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @RolesAllowed("login:head")
    public Response check(@NotBlank @PathParam("login") String login,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) {

        if (!loginService.exist(login)) {

            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.status(Status.NO_CONTENT).build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @ApiResponses(value = {

            @ApiResponse(description = "login is created", responseCode = "201", headers = {
                    @Header(name = "Location", description = "Links to Login Data", schema = @Schema(type = "string")) })

    })
    @RolesAllowed("login:create")
    public Response create(@NotNull @Parameter(schema = @Schema(ref = LOGIN_SCHEMA)) JsonNode source,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam, @Context UriInfo uriInfo)
            throws LoginServiceException {

        loginService.save(source, schemaBeanParam.getApp(), schemaBeanParam.getVersion());

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(source.get(LoginField.LOGIN.field).textValue());
        return Response.created(builder.build()).build();

    }

    @PATCH
    @Path("/{login}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Login is updated", responseCode = "204"),
            @ApiResponse(description = "Login is not found", responseCode = "404"),
            @ApiResponse(description = "Login is not accessible", responseCode = "403")

    })
    @RolesAllowed("login:update")
    public Response update(@NotNull @PathParam("login") String login, @NotNull @Parameter(schema = @Schema(name = "Patch")) ArrayNode patch,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws LoginServiceException {

        authorizationContextService.verifyLogin(login, (ApiSecurityContext) servletContext.getSecurityContext());

        JsonNode source = loginService.get(login, schemaBeanParam.getApp(), schemaBeanParam.getVersion());

        schemaValidation.validatePatch(patch);

        JsonNode data = PatchUtils.diff(patch, source);

        loginService.save(login, data, schemaBeanParam.getApp(), schemaBeanParam.getVersion());

        return Response.status(Status.NO_CONTENT).build();

    }

    @GET
    @Path("/{login}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Login Data", responseCode = "200", content = @Content(schema = @Schema(ref = LOGIN_SCHEMA))),
            @ApiResponse(description = "Login is not found", responseCode = "404"),
            @ApiResponse(description = "Login is not accessible", responseCode = "403")

    })
    @RolesAllowed("login:read")
    public JsonNode get(@NotNull @PathParam("login") String login,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws LoginServiceException {

        authorizationContextService.verifyLogin(login, (ApiSecurityContext) servletContext.getSecurityContext());

        return loginService.get(login, schemaBeanParam.getApp(), schemaBeanParam.getVersion());

    }

    @DELETE
    @Path("/{login}")
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @RolesAllowed("login:delete")
    public void delete(@NotNull @PathParam("login") String login) {

        authorizationContextService.verifyLogin(login, (ApiSecurityContext) servletContext.getSecurityContext());

        loginService.delete(login);

    }

    @Provider
    public static class LoginServiceNotFoundExceptionMapper implements ExceptionMapper<LoginServiceNotFoundException> {

        @Override
        public Response toResponse(LoginServiceNotFoundException ex) {

            return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();

        }

    }
}
