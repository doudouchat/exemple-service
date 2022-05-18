package com.exemple.service.api.stock;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.application.common.model.ApplicationDetail;
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
import lombok.RequiredArgsConstructor;

@Path("/v1/stocks")
@OpenAPIDefinition(tags = @Tag(name = "stock"))
@Component
@RequiredArgsConstructor
public class StockApi {

    private final StockService service;

    private final ApplicationDetailService applicationDetailService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{store}/{product}")
    @Operation(tags = "stock", security = { @SecurityRequirement(name = DocumentApiResource.BEARER_AUTH),
            @SecurityRequirement(name = DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS) })
    @RolesAllowed("stock:update")
    public Long post(@PathParam("store") String store, @PathParam("product") String product,
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam,
            @NotNull @Valid @Parameter(schema = @Schema(implementation = Stock.class)) Stock stock) throws InsufficientStockException {

        String application = applicationBeanParam.app;
        ApplicationDetail applicationDetail = applicationDetailService.get(application)
                .orElseThrow(() -> new NotFoundApplicationException(application));

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
            @Valid @BeanParam @Parameter(in = ParameterIn.HEADER) ApplicationBeanParam applicationBeanParam) {

        String application = applicationBeanParam.app;
        ApplicationDetail applicationDetail = applicationDetailService.get(application)
                .orElseThrow(() -> new NotFoundApplicationException(application));

        Long amount = service.get("/" + applicationDetail.getCompany(), "/" + store, "/" + product).orElseThrow(NotFoundException::new);

        return Stock.builder().amount(amount).build();

    }

    @Provider
    public static class InsufficientStockExceptionMapper implements ExceptionMapper<InsufficientStockException> {

        @Override
        public Response toResponse(InsufficientStockException e) {

            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

    }
}
