package com.exemple.service.api.stock;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.core.check.AppAndVersionCheck;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.api.stock.model.Stock;
import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.stock.StockService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Path("/v1/stocks")
@OpenAPIDefinition(tags = @Tag(name = "stock"))
@Component
@RequiredArgsConstructor
public class StockApi {

    private final StockService service;

    private final ApplicationDetailService applicationDetailService;

    @Context
    private ContainerRequestContext requestContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{store}/{product}")
    @Operation(tags = "stock", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = ApplicationBeanParam.class))
    @RolesAllowed("stock:update")
    @AppAndVersionCheck(optionalVersion = true)
    public Long post(@PathParam("store") String store, @PathParam("product") String product,
            @NotNull @Valid @Parameter(schema = @Schema(implementation = Stock.class)) Stock stock) throws InsufficientStockException {

        var application = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        var applicationDetail = applicationDetailService.get(application).orElseThrow(() -> new NotFoundApplicationException(application));

        return service.update("/" + applicationDetail.getCompany(), "/" + store, "/" + product, stock.getIncrement());

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{store}/{product}")
    @Operation(tags = "stock", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @ApiResponse(description = "Stock Data", responseCode = "200", content = @Content(schema = @Schema(implementation = Stock.class)))
    @Parameter(in = ParameterIn.HEADER, schema = @Schema(implementation = ApplicationBeanParam.class))
    @RolesAllowed("stock:read")
    @AppAndVersionCheck(optionalVersion = true)
    public Stock get(@PathParam("store") String store, @PathParam("product") String product) {

        var application = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        var applicationDetail = applicationDetailService.get(application).orElseThrow(() -> new NotFoundApplicationException(application));

        Long amount = service.get("/" + applicationDetail.getCompany(), "/" + store, "/" + product).orElseThrow(NotFoundException::new);

        return Stock.builder().amount(amount).store(store).product(product).build();

    }

    @Provider
    public static class InsufficientStockExceptionMapper implements ExceptionMapper<InsufficientStockException> {

        @Override
        public Response toResponse(InsufficientStockException e) {

            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

    }
}
