package com.exemple.service.api.subscription;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.schema.FilterHelper;
import com.exemple.service.api.common.schema.ValidationHelper;
import com.exemple.service.api.common.script.CustomerScriptFactory;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;

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

@Path("/v1/subscriptions")
@OpenAPIDefinition(tags = @Tag(name = "subscription"))
@Component
@RequiredArgsConstructor
public class SubscriptionApi {

    private static final String SUBSCRIPTION_SCHEMA = "Subscription";

    private static final String SUBSCRIPTION_RESOURCE = "subscription";

    private static final String SUBSCRIPTION_BEAN = "subscriptionService";

    private final CustomerScriptFactory scriptFactory;

    private final ValidationHelper schemaValidation;

    private final FilterHelper schemaFilter;

    @Context
    private ContainerRequestContext servletContext;

    @GET
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "subscription", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_PASS) })
    @ApiResponses(value = {

            @ApiResponse(description = "Subscription Data", responseCode = "200", content = @Content(schema = @Schema(ref = SUBSCRIPTION_SCHEMA))),
            @ApiResponse(description = "Subscription is not accessible", responseCode = "403")

    })
    @RolesAllowed("subscription:read")
    public JsonNode get(@NotNull @PathParam("email") String email,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam) {

        JsonNode subscription = scriptFactory.getBean(SUBSCRIPTION_BEAN, SubscriptionService.class).get(email).orElseThrow(NotFoundException::new);

        return schemaFilter.filter(subscription, SUBSCRIPTION_RESOURCE, servletContext);

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
    @RolesAllowed("subscription:update")
    public Response update(@NotNull @PathParam("email") String email,
            @NotNull @Parameter(schema = @Schema(ref = SUBSCRIPTION_SCHEMA)) JsonNode subscription,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) SchemaBeanParam schemaBeanParam, @Context UriInfo uriInfo) {

        JsonNodeUtils.set(subscription, email, SubscriptionField.EMAIL.field);
        schemaValidation.validate(subscription, SUBSCRIPTION_RESOURCE, servletContext);

        boolean created = scriptFactory.getBean(SUBSCRIPTION_BEAN, SubscriptionService.class).save(subscription);

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(email);
        return Response.status(created ? Status.CREATED : Status.NO_CONTENT).location(builder.build()).build();

    }

}
