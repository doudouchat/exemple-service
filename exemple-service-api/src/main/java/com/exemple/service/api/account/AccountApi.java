package com.exemple.service.api.account;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.json.JsonUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.schema.SchemaFilter;
import com.exemple.service.api.common.schema.SchemaValidation;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.api.core.check.AppAndVersionCheck;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.common.validator.NotEmpty;
import com.exemple.service.resource.account.AccountField;
import com.exemple.service.schema.validation.annotation.Patch;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;

@Path("/v1/accounts")
@OpenAPIDefinition(tags = @Tag(name = "account"))
@Component
@RequiredArgsConstructor
public class AccountApi {

    private static final String ACCOUNT_SCHEMA = "Account";

    private static final String ACCOUNT_RESOURCE = "account";

    @Context
    private AccountService accountService;

    @Context
    private AuthorizationCheckService authorizationCheckService;

    @Context
    private SchemaValidation schemaValidation;

    @Context
    private SchemaFilter schemaFilter;

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
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = SchemaBeanParam.class))
    @RolesAllowed("account:read")
    @AppAndVersionCheck
    public JsonNode get(@NotNull @PathParam("id") UUID id) {

        authorizationCheckService.verifyAccountId(id);

        JsonNode account = accountService.get(id).orElseThrow(NotFoundException::new);

        ObjectNode response = (ObjectNode) schemaFilter.filter(account, ACCOUNT_RESOURCE);
        response.remove(AccountField.ID.field);
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "account", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Account is created", responseCode = "201", headers = {
                    @Header(name = "Location", description = "Links to Account Data", schema = @Schema(type = "string")) })

    })
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = SchemaBeanParam.class))
    @RolesAllowed("account:create")
    @AppAndVersionCheck
    public Response create(@NotNull @Parameter(schema = @Schema(ref = ACCOUNT_SCHEMA)) JsonNode account, @Context UriInfo uriInfo) {

        schemaValidation.validate(account, ACCOUNT_RESOURCE);

        JsonNode source = accountService.save(account);

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
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = SchemaBeanParam.class))
    @RolesAllowed("account:update")
    @AppAndVersionCheck
    public Response update(@NotNull @PathParam("id") UUID id, @NotEmpty @Patch @Parameter(schema = @Schema(name = "Patch")) ArrayNode patch) {

        authorizationCheckService.verifyAccountId(id);

        JsonNode previousSource = accountService.get(id).orElseThrow(NotFoundException::new);

        schemaValidation.validate(patch, previousSource, ACCOUNT_RESOURCE);

        JsonNode source = JsonPatch.apply(patch, previousSource);

        accountService.save(source, previousSource);

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
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = SchemaBeanParam.class))
    @RolesAllowed("account:update")
    @AppAndVersionCheck
    public Response update(@NotNull @PathParam("id") UUID id, @NotNull @Parameter(schema = @Schema(ref = ACCOUNT_SCHEMA)) JsonNode account)
            throws IOException {

        authorizationCheckService.verifyAccountId(id);

        JsonNode previousSource = accountService.get(id).orElseThrow(NotFoundException::new);

        ((ObjectNode) account).put(AccountField.ID.field, id.toString());
        schemaValidation.validate(account, previousSource, ACCOUNT_RESOURCE);

        JsonNode accountFinal = JsonUtils.merge(account, schemaFilter.filterAllAdditionalProperties(previousSource, ACCOUNT_RESOURCE));
        accountService.save(accountFinal, previousSource);

        return Response.status(Status.NO_CONTENT).build();

    }
}
