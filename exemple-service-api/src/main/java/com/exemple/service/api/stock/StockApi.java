package com.exemple.service.api.stock;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.api.stock.model.Stock;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.common.NoFoundStockException;
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

@Path("/v1/stocks")
@OpenAPIDefinition(tags = @Tag(name = "stock"))
@Component
public class StockApi {

    private final StockService service;

    private final ApplicationDetailService applicationDetailService;

    public StockApi(StockService service, ApplicationDetailService applicationDetailService) {

        this.service = service;
        this.applicationDetailService = applicationDetailService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{store}/{product}")
    @Operation(tags = "stock", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @RolesAllowed("stock:update")
    public Long post(@PathParam("store") String store, @PathParam("product") String product,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam,
            @NotNull @Valid @Parameter(schema = @Schema(implementation = Stock.class)) Stock stock) throws InsufficientStockException {

        ApplicationDetail applicationDetail = applicationDetailService.get(applicationBeanParam.getApp());

        return service.update("/" + applicationDetail.getCompany(), "/" + store, "/" + product, stock.getIncrement());

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{store}/{product}")
    @Operation(tags = "stock", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @ApiResponse(description = "Stock Data", responseCode = "200", content = @Content(schema = @Schema(implementation = Stock.class)))
    @RolesAllowed("stock:read")
    public Stock get(@PathParam("store") String store, @PathParam("product") String product,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) throws NoFoundStockException {

        Stock stock = new Stock();

        ApplicationDetail applicationDetail = applicationDetailService.get(applicationBeanParam.getApp());

        stock.setAmount(service.get("/" + applicationDetail.getCompany(), "/" + store, "/" + product));

        return stock;

    }

    @Provider
    public static class InsufficientStockExceptionMapper implements ExceptionMapper<InsufficientStockException> {

        @Override
        public Response toResponse(InsufficientStockException e) {

            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

    }

    @Provider
    public static class NoFoundStockExceptionMapper implements ExceptionMapper<NoFoundStockException> {

        @Override
        public Response toResponse(NoFoundStockException e) {

            return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }
}
