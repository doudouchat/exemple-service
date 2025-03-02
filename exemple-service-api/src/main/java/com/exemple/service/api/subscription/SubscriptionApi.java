package com.exemple.service.api.subscription;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.json.JsonUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.schema.SchemaFilter;
import com.exemple.service.api.common.schema.SchemaValidation;
import com.exemple.service.api.core.check.AppAndVersionCheck;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.context.SubscriptionContextExecution;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;

@Path("/v1/subscriptions")
@OpenAPIDefinition(tags = @Tag(name = "subscription"))
@Component
@RequiredArgsConstructor
public class SubscriptionApi {

    private static final String SUBSCRIPTION_SCHEMA = "Subscription";

    private static final String SUBSCRIPTION_RESOURCE = "subscription";

    @Context
    private SubscriptionService subscriptionService;

    @Context
    private SchemaValidation schemaValidation;

    @Context
    private SchemaFilter schemaFilter;

    @GET
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "subscription", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {
            @ApiResponse(description = "Subscription Data", responseCode = "200", content = @Content(schema = @Schema(ref = SUBSCRIPTION_SCHEMA))),
            @ApiResponse(description = "Subscription is not accessible", responseCode = "403")
    })
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = SchemaBeanParam.class))
    @RolesAllowed("subscription:read")
    @AppAndVersionCheck
    public JsonNode get(@NotNull @PathParam("email") String email) {

        var response = subscriptionService.get(email)
                .map(subscription -> schemaFilter.filter(subscription, SUBSCRIPTION_RESOURCE))
                .map(ObjectNode.class::cast)
                .orElseThrow(NotFoundException::new);
        response.remove(SubscriptionField.EMAIL.field);

        return response;

    }

    @PUT
    @Path("/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "subscription", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {
            @ApiResponse(description = "Subscription is created", responseCode = "201", headers = {
                    @Header(name = "Location", description = "Links to Subscription Data", schema = @Schema(type = "string")) }),
            @ApiResponse(description = "Subscription is updated", responseCode = "204", headers = {
                    @Header(name = "Location", description = "Links to Subscription Data", schema = @Schema(type = "string")) })
    })
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = SchemaBeanParam.class))
    @RolesAllowed("subscription:update")
    @AppAndVersionCheck
    public Response update(@NotNull @PathParam("email") String email,
            @NotNull @Parameter(schema = @Schema(ref = SUBSCRIPTION_SCHEMA)) JsonNode value, @Context UriInfo uriInfo) throws IOException {

        var subscription = ((ObjectNode) value.deepCopy()).put(SubscriptionField.EMAIL.field, email);

        var previousSubscription = subscriptionService.get(email).map(ObjectNode.class::cast).orElse(null);

        if (previousSubscription == null) {
            schemaValidation.validate(subscription, SUBSCRIPTION_RESOURCE);
            subscriptionService.create(email, value);
            return Response.status(Status.CREATED).build();
        }

        SubscriptionContextExecution.setPreviousSubscription(previousSubscription);

        schemaValidation.validate(subscription, SUBSCRIPTION_RESOURCE);
        var additionalProperties = JsonUtils.merge(value,
                schemaFilter.filterAllAdditionalAndReadOnlyProperties(previousSubscription, SUBSCRIPTION_RESOURCE));
        subscriptionService.update(email, additionalProperties);
        return Response.status(Status.NO_CONTENT).build();

    }

}
