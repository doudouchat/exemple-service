package com.exemple.service.api.login;

import java.util.Collections;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.NotFoundException;
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
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.api.login.model.LoginModel;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.login.model.LoginEntity;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.fasterxml.jackson.core.JsonPointer;

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
import lombok.RequiredArgsConstructor;

@Path("/v1/logins")
@OpenAPIDefinition(tags = @Tag(name = "login"))
@Component
@RequiredArgsConstructor
public class LoginApi {

    private static final String LOGIN_SCHEMA = "Login";

    private final LoginResource loginResource;

    private final AuthorizationCheckService authorizationCheckService;

    @Context
    private ContainerRequestContext servletContext;

    @HEAD
    @Path("/{username}")
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @RolesAllowed("login:head")
    public Response check(@PathParam("username") String username,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) {

        if (loginResource.get(username).isPresent()) {

            return Response.status(Status.NO_CONTENT).build();
        }

        return Response.status(Status.NOT_FOUND).build();

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
    public Response create(@Parameter @Valid @NotNull LoginModel source,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam, @Context UriInfo uriInfo)
            throws UsernameAlreadyExistsException {

        LoginEntity entity = toLoginEntity(source);

        loginResource.save(entity);

        UriBuilder builder = uriInfo.getBaseUriBuilder();
        builder.path("v1/logins/" + entity.getUsername());
        return Response.created(builder.build()).build();

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
    public LoginModel get(@NotNull @PathParam("username") String username,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) {

        authorizationCheckService.verifyLogin(username, (ApiSecurityContext) servletContext.getSecurityContext());

        LoginEntity entity = loginResource.get(username).orElseThrow(NotFoundException::new);

        return toLoginModel(entity);

    }

    @DELETE
    @Path("/{username}")
    @Operation(tags = "login", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @RolesAllowed("login:delete")
    public void delete(@NotNull @PathParam("username") String username,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) {

        authorizationCheckService.verifyLogin(username, (ApiSecurityContext) servletContext.getSecurityContext());

        loginResource.delete(username);

    }

    @Provider
    public static class UsernameAlreadyExistsExceptionMapper implements ExceptionMapper<UsernameAlreadyExistsException> {

        @Override
        public Response toResponse(UsernameAlreadyExistsException exception) {

            ValidationExceptionModel cause = new ValidationExceptionModel(JsonPointer.compile("/username"), "username",
                    "[".concat(exception.getUsername()).concat("] already exists"));

            return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(Collections.singletonList(cause)).build();

        }

    }

    private static LoginEntity toLoginEntity(LoginModel resource) {

        LoginEntity entity = new LoginEntity();
        entity.setUsername(resource.getUsername());
        entity.setId(resource.getId());

        return entity;

    }

    private static LoginModel toLoginModel(LoginEntity resource) {

        return LoginModel.builder().username(resource.getUsername()).id(resource.getId()).build();

    }

}
