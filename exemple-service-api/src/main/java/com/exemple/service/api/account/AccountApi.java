package com.exemple.service.api.account;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.schema.FilterHelper;
import com.exemple.service.api.common.schema.SchemaHelper;
import com.exemple.service.api.common.schema.ValidationHelper;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.resource.account.AccountField;
import com.exemple.service.resource.common.validator.NotEmpty;
import com.exemple.service.schema.validation.annotation.Patch;
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

@Path("/v1/accounts")
@OpenAPIDefinition(tags = @Tag(name = "account"))
@Component
public class AccountApi {

    private static final String ACCOUNT_SCHEMA = "Account";

    private static final String ACCOUNT_RESOURCE = "account";

    private final AccountService service;

    private final ValidationHelper schemaValidation;

    private final FilterHelper schemaFilter;

    private final SchemaHelper schemaMerge;

    private final AuthorizationCheckService authorizationCheckService;

    @Context
    private ContainerRequestContext servletContext;

    public AccountApi(AccountService service, ValidationHelper schemaValidation, FilterHelper schemaFilter, SchemaHelper schemaMerge,
            AuthorizationCheckService authorizationCheckService) {

        this.service = service;
        this.schemaValidation = schemaValidation;
        this.schemaFilter = schemaFilter;
        this.schemaMerge = schemaMerge;
        this.authorizationCheckService = authorizationCheckService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "account", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account Data", responseCode = "200", content = @Content(schema = @Schema(ref = ACCOUNT_SCHEMA))),
            @ApiResponse(description = "Account is not found", responseCode = "404"),
            @ApiResponse(description = "Account is not accessible", responseCode = "403")

    })
    @RolesAllowed("account:read")
    public JsonNode get(@NotNull @PathParam("id") UUID id, @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam)
            throws AccountServiceNotFoundException {

        authorizationCheckService.verifyAccountId(id, (ApiSecurityContext) servletContext.getSecurityContext());

        JsonNode account = service.get(id);

        return schemaFilter.filter(account, ACCOUNT_RESOURCE, servletContext);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "account", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account is created", responseCode = "201", headers = {
                    @Header(name = "Location", description = "Links to Account Data", schema = @Schema(type = "string")) })

    })
    @RolesAllowed("account:create")
    public Response create(@NotNull @Parameter(schema = @Schema(ref = ACCOUNT_SCHEMA)) JsonNode account,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam, @Context UriInfo uriInfo) {

        schemaValidation.validate(account, ACCOUNT_RESOURCE, servletContext);

        JsonNode source = service.save(account);

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(source.get(AccountField.ID.field).textValue());
        return Response.created(builder.build()).build();

    }

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "account", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account is updated", responseCode = "204"),
            @ApiResponse(description = "Account is not found", responseCode = "404"),
            @ApiResponse(description = "Account is not accessible", responseCode = "403")

    })
    @RolesAllowed("account:update")
    public Response update(@NotNull @PathParam("id") UUID id, @NotEmpty @Patch @Parameter(schema = @Schema(name = "Patch")) ArrayNode patch,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws AccountServiceNotFoundException {

        authorizationCheckService.verifyAccountId(id, (ApiSecurityContext) servletContext.getSecurityContext());

        JsonNode previousSource = service.get(id);

        JsonNode source = JsonPatch.apply(patch, previousSource);

        schemaValidation.validate(source, previousSource, ACCOUNT_RESOURCE, servletContext);

        service.save(source, previousSource);

        return Response.status(Status.NO_CONTENT).build();

    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "account", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account is updated", responseCode = "204"),
            @ApiResponse(description = "Account is not found", responseCode = "404"),
            @ApiResponse(description = "Account is not accessible", responseCode = "403")

    })
    @RolesAllowed("account:update")
    public Response update(@NotNull @PathParam("id") UUID id, @NotNull @Parameter(schema = @Schema(ref = ACCOUNT_SCHEMA)) JsonNode account,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws AccountServiceNotFoundException {

        authorizationCheckService.verifyAccountId(id, (ApiSecurityContext) servletContext.getSecurityContext());

        JsonNode previousSource = service.get(id);

        schemaMerge.complete(account, previousSource, ACCOUNT_RESOURCE, servletContext);

        schemaValidation.validate(account, previousSource, ACCOUNT_RESOURCE, servletContext);

        service.save(account, previousSource);

        return Response.status(Status.NO_CONTENT).build();

    }

    @Provider
    public static class AccountServiceNotFoundExceptionMapper implements ExceptionMapper<AccountServiceNotFoundException> {

        @Override
        public Response toResponse(AccountServiceNotFoundException ex) {

            return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();

        }

    }
}
