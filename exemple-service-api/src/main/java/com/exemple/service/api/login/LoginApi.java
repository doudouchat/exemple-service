package com.exemple.service.api.login;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.schema.FilterHelper;
import com.exemple.service.api.common.schema.ValidationHelper;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.common.validator.NotEmpty;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.exemple.service.schema.validation.annotation.Patch;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

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

    private static final String LOGIN_RESOURCE = "login";

    private final LoginService loginService;

    private final ValidationHelper schemaValidation;

    private final FilterHelper schemaFilter;

    private final AuthorizationCheckService authorizationCheckService;

    @Context
    private ContainerRequestContext servletContext;

    public LoginApi(LoginService loginService, ValidationHelper schemaValidation, FilterHelper schemaFilter,
            AuthorizationCheckService authorizationCheckService) {

        this.loginService = loginService;
        this.schemaValidation = schemaValidation;
        this.schemaFilter = schemaFilter;
        this.authorizationCheckService = authorizationCheckService;
    }

    @HEAD
    @Path("/{username}")
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @RolesAllowed("login:head")
    public Response check(@NotBlank @PathParam("username") String username,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) throws LoginServiceException {

        if (!loginService.exist(username)) {

            throw new LoginServiceNotFoundException();
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
            throws LoginServiceAlreadyExistException {

        schemaValidation.validate(source, LOGIN_RESOURCE, servletContext);

        loginService.save(source);

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(source.get(LoginField.USERNAME.field).textValue());
        return Response.created(builder.build()).build();

    }

    @PATCH
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Login is updated", responseCode = "204"),
            @ApiResponse(description = "Login is not found", responseCode = "404"),
            @ApiResponse(description = "Login is not accessible", responseCode = "403")

    })
    @RolesAllowed("login:update")
    public Response update(@NotNull @PathParam("username") String username,
            @NotEmpty @Patch @Parameter(schema = @Schema(name = "Patch")) ArrayNode patch,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws LoginServiceException {

        authorizationCheckService.verifyLogin(username, (ApiSecurityContext) servletContext.getSecurityContext());

        JsonNode previousSource = loginService.get(username);

        JsonNode source = JsonPatch.apply(patch, previousSource);

        schemaValidation.validate(source, previousSource, LOGIN_RESOURCE, servletContext);

        loginService.save(username, source, previousSource);

        return Response.status(Status.NO_CONTENT).build();

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
    @RolesAllowed("login:read")
    public JsonNode get(@NotNull @PathParam("username") String username,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws LoginServiceException {

        authorizationCheckService.verifyLogin(username, (ApiSecurityContext) servletContext.getSecurityContext());

        JsonNode login = loginService.get(username);

        return schemaFilter.filter(login, LOGIN_RESOURCE, servletContext);

    }

    @GET
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Login Data", responseCode = "200", content = @Content(schema = @Schema(ref = LOGIN_SCHEMA))),
            @ApiResponse(description = "Login is not found", responseCode = "404"),
            @ApiResponse(description = "Login is not accessible", responseCode = "403")

    })
    @RolesAllowed("login:read")
    public List<JsonNode> getById(@NotNull @PathParam("id") UUID id,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws LoginServiceNotFoundException {

        authorizationCheckService.verifyAccountId(id, (ApiSecurityContext) servletContext.getSecurityContext());

        List<JsonNode> logins = loginService.get(id);

        return logins.stream().map(login -> schemaFilter.filter(login, LOGIN_RESOURCE, servletContext)).collect(Collectors.toList());

    }

    @DELETE
    @Path("/{username}")
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @RolesAllowed("login:delete")
    public void delete(@NotNull @PathParam("username") String username,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) {

        authorizationCheckService.verifyLogin(username, (ApiSecurityContext) servletContext.getSecurityContext());

        loginService.delete(username);

    }

    @Provider
    public static class LoginServiceNotFoundExceptionMapper implements ExceptionMapper<LoginServiceNotFoundException> {

        @Override
        public Response toResponse(LoginServiceNotFoundException ex) {

            return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();

        }

    }

    @Provider
    public static class LoginServiceAlreadyExistExceptionnMapper implements ExceptionMapper<LoginServiceAlreadyExistException> {

        @Override
        public Response toResponse(LoginServiceAlreadyExistException exception) {

            ValidationExceptionModel cause = new ValidationExceptionModel(JsonPointer.compile(JsonPointer.SEPARATOR + LoginField.USERNAME.field),
                    LoginField.USERNAME.field, "[".concat(exception.getUsername()).concat("] already exists"));

            return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(Collections.singletonList(cause)).build();

        }

    }

}
