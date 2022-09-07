package com.exemple.service.api.subscription;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.json.JsonUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.schema.SchemaFilter;
import com.exemple.service.api.common.schema.SchemaValidation;
import com.exemple.service.api.core.check.AppAndVersionCheck;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

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

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

        JsonNode subscription = subscriptionService.get(email).orElseThrow(NotFoundException::new);

        ObjectNode response = (ObjectNode) schemaFilter.filter(subscription, SUBSCRIPTION_RESOURCE);
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
            @NotNull @Parameter(schema = @Schema(ref = SUBSCRIPTION_SCHEMA)) JsonNode subscription, @Context UriInfo uriInfo) throws IOException {

        JsonNode subscriptionClone = subscription.deepCopy();
        ((ObjectNode) subscriptionClone).put(SubscriptionField.EMAIL.field, email);

        ObjectNode previousSubscription = (ObjectNode) subscriptionService.get(email).orElse(null);

        if (previousSubscription == null) {
            previousSubscription = MAPPER.createObjectNode();
            previousSubscription.set("email", TextNode.valueOf(email));
            schemaValidation.validate(subscriptionClone, previousSubscription, SUBSCRIPTION_RESOURCE);
            subscriptionService.save(email, subscription);
            return Response.status(Status.CREATED).build();
        } else {
            schemaValidation.validate(subscriptionClone, previousSubscription, SUBSCRIPTION_RESOURCE);
            JsonNode subscriptionFinal = JsonUtils.merge(subscription,
                    schemaFilter.filterAllAdditionalProperties(previousSubscription, SUBSCRIPTION_RESOURCE));
            subscriptionService.save(email, subscriptionFinal, previousSubscription);
            return Response.status(Status.NO_CONTENT).build();

        }

    }

}
