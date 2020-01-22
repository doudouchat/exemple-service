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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.PatchUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationContextService;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.context.AccountContext;
import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.resource.account.AccountField;
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

@Path("/v1/accounts")
@OpenAPIDefinition(tags = @Tag(name = "account"))
@Component
public class AccountApi {

    private static final Logger LOG = LoggerFactory.getLogger(AccountApi.class);

    private static final String ACCOUNT_SCHEMA = "Account";

    @Autowired
    private AccountService service;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private AuthorizationContextService authorizationContextService;

    @Context
    private ContainerRequestContext servletContext;

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
            throws AccountServiceException {

        authorizationContextService.verifyAccountId(id, (ApiSecurityContext) servletContext.getSecurityContext());

        return service.get(id, schemaBeanParam.getApp(), schemaBeanParam.getVersion());

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
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam, @Context UriInfo uriInfo)
            throws AccountServiceException {

        JsonNode source = service.save(account, schemaBeanParam.getApp(), schemaBeanParam.getVersion());

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
    public Response update(@NotNull @PathParam("id") UUID id, @NotNull @Parameter(schema = @Schema(name = "Patch")) ArrayNode patch,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) throws AccountServiceException {

        authorizationContextService.verifyAccountId(id, (ApiSecurityContext) servletContext.getSecurityContext());

        schemaValidation.validatePatch(patch);

        JsonNode source = service.get(id, schemaBeanParam.getApp(), schemaBeanParam.getVersion());
        JsonNode account = PatchUtils.diff(patch, source);
        LOG.debug("account update {}", account);

        service.save(id, account, schemaBeanParam.getApp(), schemaBeanParam.getVersion());

        return Response.status(Status.NO_CONTENT).build();

    }

    @Provider
    public static class AccountServiceNotFoundExceptionMapper implements ExceptionMapper<AccountServiceNotFoundException> {

        @Override
        public Response toResponse(AccountServiceNotFoundException ex) {

            return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();

        }

    }

    @Provider
    public static class AccountContextResponseFilter implements ContainerResponseFilter {

        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

            AccountContext.destroy();

        }

    }
}
